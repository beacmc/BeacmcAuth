package com.beacmc.beacmcauth.core.auth;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.AuthenticatingPlayer;
import com.beacmc.beacmcauth.api.auth.action.AuthActionTypes;
import com.beacmc.beacmcauth.api.auth.premium.PremiumChangerProvider;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.auth.protection.ProtectionManager;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.cache.ConcurrentCache;
import com.beacmc.beacmcauth.api.config.AccountLimiterSettings;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.crypto.CryptoRegistry;
import com.beacmc.beacmcauth.api.crypto.hash.Hash;
import com.beacmc.beacmcauth.api.crypto.hash.HashedPassword;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.beacmc.beacmcauth.api.dialog.custom.DialogType;
import com.beacmc.beacmcauth.api.event.type.AuthPremiumLoginEvent;
import com.beacmc.beacmcauth.api.event.type.AuthSessionActiveEvent;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.model.AltAccounts;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.api.song.SongManager;
import com.beacmc.beacmcauth.core.util.FutureUtil;
import com.beacmc.beacmcauth.core.util.runnable.LoginRunnable;
import com.beacmc.beacmcauth.core.util.runnable.RegisterRunnable;
import com.j256.ormlite.stmt.SelectArg;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseAuthManager implements AuthManager {

    private final BeacmcAuth plugin;
    private final Cache<AuthenticatingPlayer, String> authenticatingPlayersCache;
    private final Cache<PremiumPlayer, String> premiumPlayers;
    private final ProtectedPlayerDao dao;
    private final Cache<AltAccounts, String> altAccountsCache;
    private final ServerLogger logger;
    @Getter
    private final Cache<ProtectedPlayer, UUID> playerCache;
    @Getter
    private final ExecutorService executorService;

    public BaseAuthManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.altAccountsCache = new ConcurrentCache<>();
        this.executorService = plugin.getExecutorService();
        this.authenticatingPlayersCache = new ConcurrentCache<>();
        this.logger = plugin.getServerLogger();
        this.playerCache = plugin.getDatabase().getPlayersCache();
        this.dao = plugin.getDatabase().getProtectedPlayerDao();
        this.premiumPlayers = new ConcurrentCache<>();
    }

    @Override
    public void onServerConnect(ServerPlayer player) {
        plugin.getProxy().runTaskLater(() -> {
            if (!isAuthenticating(player)) {
                return;
            }

            final DialogManager dialogManager = plugin.getDialogManager();

            SongManager songManager = plugin.getSongManager();
            songManager.play(player, songManager.findRandomSong());
            AuthenticatingPlayer authPlayer = authenticatingPlayersCache.getCacheData(player.getLowercaseName());

            if (!dialogManager.canShowDialog(player) || authPlayer == null) {
                return;
            }

            ProtectedPlayer protectedPlayer = authPlayer.getPlayer();

            boolean canEmailRecovery = plugin.getEmailConfig().isEnabled()
                    && protectedPlayer.getEmail() != null;
            boolean canSecretQuestionRecovery = protectedPlayer.getSecretQuestion() != null
                    && protectedPlayer.getHashedSecretAnswer() != null;

            if (canEmailRecovery && !canSecretQuestionRecovery) {
                player.showDialog(dialogManager.getDialog(DialogType.CHOOSE_DIALOG_WITHOUT_SECRET_QUESTION));
                return;
            } else if (!canEmailRecovery && canSecretQuestionRecovery) {
                player.showDialog(dialogManager.getDialog(DialogType.CHOOSE_DIALOG_WITHOUT_EMAIL));
                return;
            } else if (canEmailRecovery) {
                player.showDialog(dialogManager.getDialog(DialogType.CHOOSE_DIALOG_FULL));
                return;
            }

            if (protectedPlayer.isRegister()) {
                player.showDialog(dialogManager.getDialog(DialogType.LOGIN));
            } else {
                player.showDialog(dialogManager.getDialog(DialogType.REGISTER));
            }
        }, 100L, TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<Server> onConnect(@Nullable ServerPlayer player) {
        if (player == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("ServerPlayer is null"));
        }

        final Config config = plugin.getConfig();
        final InetAddress address = player.getInetAddress();
        final AccountLimiterSettings accountLimiter = config.getAccountLimiterSettings();
        final ProtectionManager protectionManager = plugin.getProtectionManager();
        final ConfigMessages messages = config.getMessages();

        if (address == null || isAuthenticating(player)) {
            player.disconnect(messages.getInternalError());
            return CompletableFuture.failedFuture(new IllegalStateException("Player is authenticating or have null address"));
        }

        if (!config.getNicknameRegex().matcher(player.getName()).matches()) {
            player.disconnect(messages.getInvalidCharacterInName());
            return CompletableFuture.failedFuture(new SecurityException("The name does not match the parameters."));
        }

        if (protectionManager.isBlockedAction(player, AuthActionTypes.LOGIN)) {
            player.disconnect(messages.getLoginBlockedDisconnect());
            return CompletableFuture.failedFuture(new IllegalStateException("Server access is unavailable."));
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
                    player.disconnect(messages.getInternalError());
                    return CompletableFuture.failedFuture(new IllegalStateException("Unable to find ProtectedPlayer for player " + player));
                }

                if (protectedPlayer.isBanned()) {
                    player.disconnect(messages.getAccountBanned());
                    return CompletableFuture.failedFuture(new SecurityException("Player is banned"));
                }

                if (config.isNameCaseControl() && !protectedPlayer.getRealName().equals(player.getName())) {
                    player.disconnect(messages.getNameCaseFailed()
                            .replace("%current_name%", player.getName())
                            .replace("%need_name%", protectedPlayer.getRealName()));
                    return CompletableFuture.failedFuture(new SecurityException("The nicknames don't match"));
                }

                if (!protectedPlayer.isRegister()) {
                    if (protectionManager.isBlockedAction(player, AuthActionTypes.REGISTER)) {
                        player.disconnect(messages.getRegisterBlockedDisconnect());
                        return CompletableFuture.failedFuture(new IllegalStateException("Registration is unavailable."));
                    }

                    AltAccounts altAccounts = FutureUtil.await(getAltAccounts(address.getHostAddress()));
                    if (accountLimiter.isEnabled() && altAccounts != null && altAccounts.getNames().size() >= accountLimiter.getLimit()) {
                        player.disconnect(messages.getAlternativeAccountsLimitReached());
                        return CompletableFuture.failedFuture(new SecurityException("the limit of alternative accounts has been reached."));
                    }

                    logger.debug("The player(" + player.getName() + ") has started registration");

                    authenticatingPlayersCache.addOrUpdateCache(new AuthenticatingPlayer(
                            protectedPlayer,
                            config.getPasswordAttempts()
                    ));
                    new RegisterRunnable(plugin, player);
                    addAltAccount(protectedPlayer, address.getHostAddress());
                    return CompletableFuture.completedFuture(config.findServer(config.getAuthServers()));
                }

                PremiumPlayer premiumPlayer = getOnlinePremiumPlayer(player.getLowercaseName());
                if (premiumPlayer != null) {
                    if (premiumPlayer.isTemplate()) {
                        UUID onlineUuid = premiumPlayer.getUniqueId();
                        if (onlineUuid != null) {
                            protectedPlayer.setOnlineUuid(onlineUuid);
                        }
                        premiumPlayers.addOrUpdateCache(new PremiumPlayer(premiumPlayer.getLowercaseName(), onlineUuid, false, 0));
                    }
                    logger.debug("Automatic login for player(%s) with premium status".formatted(player.getName()));
                    player.sendMessage(messages.getPremiumAccountAutoLogin());

                    plugin.getEventManager().fire(new AuthPremiumLoginEvent(protectedPlayer, player));

                    performLogin(protectedPlayer
                            .setLowercaseName(player.getLowercaseName())
                            .setRealName(player.getName()));
                    return CompletableFuture.completedFuture(config.findServer(config.getLobbyServers()));
                }

                logger.debug("Checking the player(" + player.getName() + ") for an active session(" + config.getSessionTime() + ") and a match in the IP-address(" + address.getHostAddress() + ")");

                if (protectedPlayer.isSessionActive(config.getSessionTime()) && protectedPlayer.isValidIp(address.getHostAddress())) {
                    logger.debug("the player(" + player.getName() + ") has an active session and a valid IP-address(" + address.getHostAddress() + ")");

                    player.sendMessage(messages.getSessionActive());
                    plugin.getEventManager().fire(new AuthSessionActiveEvent(protectedPlayer, player));
                    return CompletableFuture.completedFuture(config.findServer(config.getLobbyServers()));
                }

                logger.debug("The player(" + player.getName() + ") has started authorization");

                authenticatingPlayersCache.addOrUpdateCache(new AuthenticatingPlayer(
                        protectedPlayer,
                        config.getPasswordAttempts()
                ));
                new LoginRunnable(plugin, player);
                return CompletableFuture.completedFuture(config.findServer(config.getAuthServers()));
            } catch (Exception e) {
                player.disconnect(messages.getInternalError());
                return CompletableFuture.failedFuture(e);
            }
        }).exceptionally(e -> {
            logger.error("BaseAuthManager#onConnect have " + e.getClass().getSimpleName());
            logger.error("Message: " + e.getMessage());
            return null;
        });
    }

    @Override
    public <T> CompletableFuture<Void> onPremiumLogin(
            String playerName,
            PremiumChangerProvider<T> premiumChangerProvider,
            T obj
    ) {
        return getProtectedPlayer(playerName).thenCompose(protectedPlayer -> {
            if (playerName == null || protectedPlayer == null) {
                return CompletableFuture.completedFuture(null);
            }

            if (plugin.getProtectionManager().isBlockedAction(playerName, AuthActionTypes.PREMIUM_AUTHENTICATION)) {
                return CompletableFuture.completedFuture(null);
            }

            if (!protectedPlayer.isRegister()) {
                return CompletableFuture.completedFuture(null);
            }

            UUID storedOnlineUuid = protectedPlayer.getOnlineUuid();
            PremiumPlayer premiumPlayer =
                    getOnlinePremiumPlayer(playerName.toLowerCase());

            if (storedOnlineUuid != null) {
                premiumPlayers.addOrUpdateCache(
                        new PremiumPlayer(playerName, storedOnlineUuid, false, 0)
                );
                premiumChangerProvider.forceOnlineMode(obj);
            } else if (premiumPlayer != null && premiumPlayer.isTemplate()) {

                if (premiumPlayer.isValidTemplateTime()) {
                    premiumChangerProvider.forceOnlineMode(obj);
                } else {
                    getPremiumPlayerCache().removeCache(premiumPlayer);
                }
            }

            return CompletableFuture.completedFuture(null);
        });
    }


    @Override
    public void onDisconnect(ServerPlayer player) {
        getAuthPlayers().removeById(player.getLowercaseName());
        PremiumPlayer premiumPlayer = premiumPlayers.getCacheData(player.getLowercaseName());

        plugin.getSongManager().stop(player.getUUID());

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
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
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
    public void connectLobbyServer(ServerPlayer player) {
        final Config config = plugin.getConfig();
        connectPlayer(player, config.findServer(config.getLobbyServers()));
    }

    @Override
    public boolean isAuthenticating(@NotNull String playerName) {
        final SocialManager socialManager = plugin.getSocialManager();
        return (getAuthPlayers().contains(playerName.toLowerCase())
                || socialManager.getConfirmationByName(playerName.toLowerCase()) != null);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> createProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, String registerIp, String lastIp, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer execute = new ProtectedPlayer(
                        lowercaseName,
                        realName,
                        uuid,
                        null,
                        password,
                        null, // FIXME
                        session,
                        lastJoin,
                        false,
                        true,
                        true,
                        registerIp,
                        lastIp,
                        null,
                        null,
                        null,
                        0,
                        0,
                        null
                );
                dao.create(execute);
                return execute;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
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
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> getProtectedPlayerByOnlineId(UUID onlineUuid) {
        if (onlineUuid == null) return null;
        return CompletableFuture.supplyAsync(() -> {

            try {
                ProtectedPlayer cachedData = playerCache.stream()
                        .filter(protectedPlayer -> onlineUuid.equals(protectedPlayer.getOnlineUuid()))
                        .findFirst()
                        .orElse(null);
                if (cachedData != null) return cachedData;

                ProtectedPlayer queryData = dao.queryBuilder()
                        .where()
                        .eq(ProtectedPlayer.ONLINE_UUID_COLUMN, new SelectArg(onlineUuid))
                        .queryForFirst();
                playerCache.addOrUpdateCache(queryData);
                return queryData;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }


    @Override
    public CompletableFuture<ProtectedPlayer> getProtectedPlayer(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer cachedData = playerCache.stream()
                        .filter(protectedPlayer -> protectedPlayer.getLowercaseName()
                                .equalsIgnoreCase(playerName))
                        .findFirst()
                        .orElse(null);
                if (cachedData != null) return cachedData;

                ProtectedPlayer queryData = dao.queryBuilder()
                        .where()
                        .eq(ProtectedPlayer.LOWERCASE_NAME_COLUMN, new SelectArg(playerName))
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
                getAuthPlayers().removeById(protectedPlayer.getLowercaseName());
                String ip = player.getInetAddress().getHostAddress();
                long currentTime = System.currentTimeMillis();
                dao.createOrUpdate(protectedPlayer
                        .setLastIp(ip)
                        .setSession(currentTime)
                        .setLastJoin(currentTime)
                );
                playerCache.addOrUpdateCache(protectedPlayer);
            } catch (Exception e) {
                player.disconnect(plugin.getConfig().getMessages().getInternalError());
                throw new CompletionException(e);
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

                List<ProtectedPlayer> players = dao.queryForEq(
                        ProtectedPlayer.REGISTER_IP_COLUMN,
                        new SelectArg(hostAddress)
                );
                AltAccounts altAccounts = new AltAccounts(hostAddress, players.stream()
                        .map(ProtectedPlayer::getRealName)
                        .collect(Collectors.toList()));
                altAccountsCache.addOrUpdateCache(altAccounts);
                return altAccounts;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    @Override
    public void addAltAccount(ProtectedPlayer player, String ip) {
        AltAccounts altAccounts = altAccountsCache.getCacheData(ip);
        String playerName = player.getRealName();
        if (altAccounts != null && !altAccounts.getNames().contains(playerName)) {
            altAccounts.getNames().add(playerName);
        }
    }

    @Override
    public CompletableFuture<Boolean> checkPassword(ProtectedPlayer player, String password) {
        HashedPassword model = player.getHashedPassword();
        String hashedPassword = model.getPassword();
        String hashId = model.getHashId();

        Hash mainHash = plugin.getConfig().getPasswordHash();
        Hash currentHash = mainHash.id().equals(hashId)
                ? mainHash
                : CryptoRegistry.INSTANCE.findById(hashId);

        if (currentHash == null) {
            return CompletableFuture.failedFuture(new CompletionException(
                    "Unknown hash type: " + hashId,
                    new IllegalArgumentException()
            ));
        }


        return currentHash.matches(hashedPassword, password)
                .thenCompose(result -> {
                    if (!result || !mainHash.isMigrateAvailable(currentHash)) {
                        return CompletableFuture.completedFuture(result);
                    }

                    return mainHash.migrate(password)
                            .thenCompose(newPassword -> {
                                player.setHashedPassword(newPassword);
                                return changePassword(player, newPassword)
                                        .thenApply(ignored -> true);
                            });
                });
    }

    @Override
    public CompletableFuture<ProtectedPlayer> changePassword(ProtectedPlayer protectedPlayer, HashedPassword hashedPassword) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                dao.createOrUpdate(protectedPlayer
                        .setSession(0)
                        .setPassword(hashedPassword));
                playerCache.addOrUpdateCache(protectedPlayer);
                logger.debug("Player(" + protectedPlayer.getRealName() + ") successfully changed password");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return protectedPlayer;
        }, executorService);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> changePassword(ProtectedPlayer protectedPlayer, String newPassword) {
        Hash hash = plugin.getConfig().getPasswordHash();
        return hash.hash(newPassword)
                .thenCompose(hashed -> changePassword(
                        protectedPlayer,
                        new HashedPassword(hash.id(), hashed)
                ));
    }

    @Override
    public CompletableFuture<ProtectedPlayer> register(ProtectedPlayer protectedPlayer, String password) {
        final Config config = plugin.getConfig();

        ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
        if (player == null) {
            return null;
        }
        Hash passwordHash = config.getPasswordHash();
        return passwordHash.hash(password)
                .thenComposeAsync(hashedPassword -> {
                    try {
                        logger.debug("Player(" + protectedPlayer.getRealName() + ") successfully registered");
                        dao.createOrUpdate(protectedPlayer
                                .setPassword(new HashedPassword(passwordHash.id(), hashedPassword)));
                        playerCache.addOrUpdateCache(protectedPlayer);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        player.disconnect(config.getMessages().getInternalError());
                    }
                    return null;
                }, executorService);
    }

    @Override
    public Cache<AuthenticatingPlayer, String> getAuthPlayers() {
        return authenticatingPlayersCache;
    }

    public PremiumPlayer getOnlinePremiumPlayer(String lowercaseName) {
        return premiumPlayers.getCacheData(lowercaseName);
    }

    public Cache<PremiumPlayer, String> getPremiumPlayerCache() {
        return premiumPlayers;
    }
}
