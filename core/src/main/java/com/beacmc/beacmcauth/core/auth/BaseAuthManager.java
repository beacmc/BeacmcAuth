package com.beacmc.beacmcauth.core.auth;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.auth.premium.mojang.PremiumChangerProvider;
import com.beacmc.beacmcauth.api.auth.premium.response.Response;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.AccountLimiterSettings;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.config.MojangAuthConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.api.model.AltAccounts;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.core.cache.AltAccountCache;
import com.beacmc.beacmcauth.core.cache.PremiumPlayerCache;
import com.beacmc.beacmcauth.core.util.FutureUtil;
import com.beacmc.beacmcauth.core.util.runnable.LoginRunnable;
import com.beacmc.beacmcauth.core.util.runnable.RegisterRunnable;
import com.j256.ormlite.stmt.SelectArg;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class BaseAuthManager implements AuthManager {

    private final BeacmcAuth plugin;
    private final Map<String, Integer> authorizationPlayers;
    private final Cache<PremiumPlayer, String> premiumPlayers;
    private final ProtectedPlayerDao dao;
    @Getter
    private final Cache<ProtectedPlayer, UUID> playerCache;
    private final Cache<AltAccounts, String> altAccountsCache;
    private final ServerLogger logger;
    @Getter
    private final ExecutorService executorService;

    public BaseAuthManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.altAccountsCache = new AltAccountCache();
        this.executorService = plugin.getExecutorService();
        this.authorizationPlayers = new HashMap<>();
        this.logger = plugin.getServerLogger();
        this.playerCache = plugin.getDatabase().getPlayersCache();
        this.dao = plugin.getDatabase().getProtectedPlayerDao();
        this.premiumPlayers = new PremiumPlayerCache();
    }

    @Override
    public CompletableFuture<Server> onConnect(@Nullable ServerPlayer player) {
        if (player == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("ServerPlayer is null"));
        }

        final Config config = plugin.getConfig();
        final InetAddress address = player.getInetAddress();
        final AccountLimiterSettings accountLimiter = config.getAccountLimiterSettings();

        if (address == null || isAuthenticating(player)) {
            player.disconnect(config.getMessages().getInternalError());
            return CompletableFuture.failedFuture(new IllegalStateException("Player is authenticating or have null address"));
        }

        logger.debug("Check player(" + player.getName() + ") nickname matcher(" + config.getNicknameRegex() + "). Success: " + config.getNicknameRegex().matcher(player.getName()).matches());

        if (!config.getNicknameRegex().matcher(player.getName()).matches()) {
            player.disconnect(config.getMessages().getInvalidCharacterInName());
            return CompletableFuture.failedFuture(new SecurityException("The name does not match the parameters."));
        }

        AltAccounts altAccounts = FutureUtil.await(getAltAccounts(address.getHostAddress()));
        if (accountLimiter.isEnabled() && altAccounts != null && altAccounts.getNames().size() >= accountLimiter.getLimit()) {
            player.disconnect(config.getMessages().getAlternativeAccountsLimitReached());
            return CompletableFuture.failedFuture(new SecurityException("the limit of alternative accounts has been reached."));
        }

        return getProtectedPlayer(player.getUUID()).thenCompose(protectedPlayer -> {
            if (protectedPlayer == null) {
                logger.debug("Create ProtectedPlayer. Username: " + player.getName());
                return createProtectedPlayer(player.getLowercaseName(), player.getName(), null, 0, System.currentTimeMillis(), address.getHostAddress(), address.getHostAddress(), player.getUUID());
            }
            return CompletableFuture.completedFuture(protectedPlayer);
        }).thenCompose(protectedPlayer -> {
            try {
                if (protectedPlayer == null) {
                    player.disconnect(config.getMessages().getInternalError());
                    return CompletableFuture.failedFuture(new IllegalStateException("Unable to find ProtectedPlayer for player " + player));
                }

                if (protectedPlayer.isBanned()) {
                    player.disconnect(config.getMessages().getAccountBanned());
                    return CompletableFuture.failedFuture(new SecurityException("Player is banned"));
                }

                if (config.isNameCaseControl() && !protectedPlayer.getRealName().equals(player.getName())) {
                    player.disconnect(config.getMessages().getNameCaseFailed()
                            .replace("%current_name%", player.getName())
                            .replace("%need_name%", protectedPlayer.getRealName()));
                    return CompletableFuture.failedFuture(new SecurityException("The nicknames don't match"));
                }

                if (!protectedPlayer.isRegister()) {
                    logger.debug("The player(" + player.getName() + ") has started registration");

                    authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
                    new RegisterRunnable(plugin, player);
                    addAltAccount(protectedPlayer, address.getHostAddress());
                    return CompletableFuture.completedFuture(config.findServer(config.getAuthServers()));
                }

                PremiumPlayer premiumPlayer = getOnlinePremiumPlayer(player.getLowercaseName());
                if (premiumPlayer != null) {
                    if (premiumPlayer.isValidTemplateTime()) {
                        UUID onlineUuid = premiumPlayer.getUniqueId();
                        if (onlineUuid != null) {
                            protectedPlayer.setOnlineUuid(onlineUuid);
                        }
                        premiumPlayers.addOrUpdateCache(new PremiumPlayer(premiumPlayer.getLowercaseName(), onlineUuid, false, 0));
                    }
                    logger.debug("Automatic login for player(%s) with premium status".formatted(player.getName()));
                    player.sendMessage(config.getMessages().getPremiumAccountAutoLogin());
                    performLogin(protectedPlayer
                            .setLowercaseName(player.getLowercaseName())
                            .setRealName(player.getName()));
                    return CompletableFuture.completedFuture(config.findServer(config.getLobbyServers()));
                }

                logger.debug("Checking the player(" + player.getName() + ") for an active session(" + config.getSessionTime() + ") and a match in the IP-address(" + address.getHostAddress() + ")");

                if (protectedPlayer.isSessionActive(config.getSessionTime()) && protectedPlayer.isValidIp(address.getHostAddress())) {
                    logger.debug("the player(" + player.getName() + ") has an active session and a valid IP-address(" + address.getHostAddress() + ")");

                    player.sendMessage(config.getMessages().getSessionActive());
                    return CompletableFuture.completedFuture(config.findServer(config.getLobbyServers()));
                }

                logger.debug("The player(" + player.getName() + ") has started authorization");

                authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
                new LoginRunnable(plugin, player);
                return CompletableFuture.completedFuture(config.findServer(config.getAuthServers()));
            } catch (Throwable e) {
                player.disconnect(config.getMessages().getInternalError());
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    @Override
    public <T> @Nullable Message onPremiumLogin(
            String playerName,
            PremiumChangerProvider<T> premiumChangerProvider,
            T obj
    ) {
        if (playerName == null) {
            return null;
        }

        final MojangAuthConfig authConfig = plugin.getMojangAuthConfig();
        final ConfigMessages messages = plugin.getConfig().getMessages();

        try {
            Response<PremiumUser> response = plugin
                    .getMojangAuthManager()
                    .getPremiumUser(playerName)
                    .join();

            PremiumUser premiumUser = response.getData();
            UUID mojangUuid = premiumUser != null ? premiumUser.getUuid() : null;

            if (premiumUser == null) {
                premiumPlayers.removeById(playerName);
                logger.debug("Premium user not found. Response: " + response);
            }

            ProtectedPlayer protectedPlayer = getProtectedPlayer(playerName).get();
            UUID storedOnlineUuid = protectedPlayer.getOnlineUuid();

            if (storedOnlineUuid == null) {
                return null;
            }

            PremiumPlayer cachedPremium =
                    getOnlinePremiumPlayer(protectedPlayer.getLowercaseName());

            boolean disallowedResponse =
                    (response.isRateLimited() && !authConfig.isLicenseJoinRateLimited())
                            || (!response.isSuccess()
                            && !authConfig.isLicenseJoinMojangDown()
                            && (cachedPremium == null || !cachedPremium.isTemplate()));

            if (disallowedResponse) {
                return plugin.getMessageProvider()
                        .createMessage(messages.getInternalError());
            }

            boolean validUuidMatch = storedOnlineUuid.equals(mojangUuid);

            boolean validTemplate =
                    cachedPremium != null && cachedPremium.isValidTemplateTime();

            if (!validUuidMatch && !validTemplate) {
                return null;
            }

            if (cachedPremium == null) {
                premiumPlayers.addOrUpdateCache(
                        new PremiumPlayer(playerName, mojangUuid, false, 0)
                );
            } else {
                cachedPremium.setUniqueId(mojangUuid);
            }

            premiumChangerProvider.forceOnlineMode(obj);

        } catch (Exception e) {
            logger.error("Error during premium login for " + playerName);
        }

        return null;
    }


    @Override
    public void onDisconnect(ServerPlayer player) {
        getAuthPlayers().remove(player.getLowercaseName());
        PremiumPlayer premiumPlayer = premiumPlayers.getCacheData(player.getLowercaseName());
        if (premiumPlayer == null || !premiumPlayer.isValidTemplateTime()) {
            premiumPlayers.removeCache(premiumPlayer);
        }

        ConfirmationPlayer confirmationPlayer = plugin.getSocialManager().getConfirmationByName(player.getLowercaseName());
        plugin.getSocialManager().getConfirmationPlayers().remove(confirmationPlayer);
    }

    public CompletableFuture<Void> saveSecretQuestion(ProtectedPlayer player, String question, String answer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                dao.createOrUpdate(player.setSecretQuestion(question, answer));
                playerCache.addOrUpdateCache(player);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executorService);
    }

    @Override
    public void connectPlayer(ServerPlayer player, Server server) {
        if (server == null || player == null) return;
        logger.debug("Try connect player(" + player + ") to server(" + server.getName() + ")");

        player.connect(server);
    }

    @Override
    public void connectAuthServer(ServerPlayer player) {
        final Config config = plugin.getConfig();
        connectPlayer(player, config.findServer(config.getAuthServers()));
    }

    @Override
    public void connectGameServer(ServerPlayer player) {
        final Config config = plugin.getConfig();
        connectPlayer(player, config.findServer(config.getLobbyServers()));
    }

    @Override
    public boolean isAuthenticating(@NotNull String playerName) {
        final SocialManager socialManager = plugin.getSocialManager();
        return (getAuthPlayers().containsKey(playerName.toLowerCase())
                || socialManager.getConfirmationByName(playerName.toLowerCase()) != null);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> createProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, String registerIp, String lastIp, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer execute = new ProtectedPlayer(lowercaseName, realName, uuid, null, password, session, lastJoin, false, true, true, true, registerIp, lastIp, null, null, null, 0, 0, 0);
                dao.create(execute);
                return execute;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executorService);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> getProtectedPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer cachedData = playerCache.getCacheData(uuid);
                if (cachedData != null) return cachedData;

                ProtectedPlayer queryData = dao.queryForId(uuid);
                playerCache.addOrUpdateCache(queryData);
                return queryData;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executorService);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> getPremiumPlayer(UUID premiumUuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (premiumUuid == null) return null;

            try {
                ProtectedPlayer cachedData = playerCache.stream()
                        .filter(protectedPlayer -> protectedPlayer.getOnlineUuid() == premiumUuid)
                        .findFirst()
                        .orElse(null);
                if (cachedData != null) return cachedData;

                ProtectedPlayer queryData = dao.queryBuilder()
                        .where()
                        .eq("online_uuid", new SelectArg(premiumUuid))
                        .queryForFirst();
                playerCache.addOrUpdateCache(queryData);
                return queryData;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executorService);
    }


    @Override
    public CompletableFuture<ProtectedPlayer> getProtectedPlayer(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer cachedData = playerCache.stream()
                        .filter(protectedPlayer -> protectedPlayer.getLowercaseName().equals(playerName))
                        .findFirst()
                        .orElse(null);
                if (cachedData != null) return cachedData;

                ProtectedPlayer queryData = dao.queryBuilder()
                        .where()
                        .eq("lowercase_name", new SelectArg(playerName))
                        .queryForFirst();
                playerCache.addOrUpdateCache(queryData);
                return queryData;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executorService);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> performLogin(ProtectedPlayer protectedPlayer) {
        return CompletableFuture.supplyAsync(() -> {

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
            if (player == null) {
                return null;
            }

            try {
                logger.debug("Player(" + player.getName() + ") successfully logged in");
                getAuthPlayers().remove(protectedPlayer.getLowercaseName());
                String ip = player.getInetAddress().getHostAddress();
                long currentTime = System.currentTimeMillis();
                dao.createOrUpdate(protectedPlayer
                        .setLastIp(ip)
                        .setSession(currentTime)
                        .setLastJoin(currentTime)

                );
                playerCache.addOrUpdateCache(protectedPlayer);
            } catch (SQLException e) {
                e.printStackTrace();
                player.disconnect(plugin.getConfig().getMessages().getInternalError());
            }
            return protectedPlayer;
        }, executorService);
    }

    @Override
    public CompletableFuture<AltAccounts> getAltAccounts(String hostAddress) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AltAccounts cached = altAccountsCache.getCacheData(hostAddress);
                if (cached != null) return cached;

                List<ProtectedPlayer> players = dao.queryForEq("reg_ip", hostAddress);
                AltAccounts altAccounts = new AltAccounts(hostAddress, players.stream()
                        .map(ProtectedPlayer::getRealName)
                        .collect(Collectors.toList()));
                altAccountsCache.addOrUpdateCache(altAccounts);
                return altAccounts;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executorService);
    }

    @Override
    public void addAltAccount(ProtectedPlayer player, String ip) {
        AltAccounts altAccounts = altAccountsCache.getCacheData(ip);
        if (altAccounts != null) {
            altAccounts.getNames().add(player.getRealName());
        }
    }

    @Override
    public CompletableFuture<ProtectedPlayer> register(ProtectedPlayer protectedPlayer, String password) {
        final Config config = plugin.getConfig();

        ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
        if (player == null) {
            return null;
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Player(" + protectedPlayer.getRealName() + ") successfully registered");
                dao.createOrUpdate(protectedPlayer.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(config.getBCryptRounds()))));
                playerCache.addOrUpdateCache(protectedPlayer);
            } catch (SQLException e) {
                e.printStackTrace();
                player.disconnect(config.getMessages().getInternalError());
            }
            return null;
        }, executorService);
    }

    @Override
    public Map<String, Integer> getAuthPlayers() {
        return authorizationPlayers;
    }

    public PremiumPlayer getOnlinePremiumPlayer(String lowercaseName) {
        return premiumPlayers.getCacheData(lowercaseName);
    }

    public Cache<PremiumPlayer, String> getPremiumPlayerCache() {
        return premiumPlayers;
    }
}
