package com.beacmc.beacmcauth.core.auth;

import com.azuriom.azlink.common.AzLinkPlatform;
import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.auth.premium.PremiumProvider;
import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.core.cache.PremiumPlayerCache;
import com.beacmc.beacmcauth.core.cache.PremiumUserCache;
import com.beacmc.beacmcauth.core.command.executor.PremiumExecutor;
import com.beacmc.beacmcauth.core.util.UuidGenerator;
import com.beacmc.beacmcauth.core.util.runnable.LoginRunnable;
import com.beacmc.beacmcauth.core.util.runnable.RegisterRunnable;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.j256.ormlite.stmt.SelectArg;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class BaseAuthManager implements AuthManager {

    private final BeacmcAuth plugin;
    private final Map<String, Integer> authorizationPlayers;
    private final Cache<PremiumPlayer, String> premiumPlayers;
    private final ProtectedPlayerDao dao;
    private final Cache<ProtectedPlayer, UUID> playerCache;
    @Getter
    private final Cache<PremiumUser, String> premiumCache;
    private final ServerLogger logger;
    private final AzLinkPlatform azLinkPlatform;
    private final ExecutorService executorService;

    public BaseAuthManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.executorService = plugin.getExecutorService();
        this.authorizationPlayers = new HashMap<>();
        this.logger = plugin.getServerLogger();
        this.playerCache = plugin.getDatabase().getPlayersCache();
        this.azLinkPlatform = plugin.getProxy().getPlugin("AzLink");
        this.dao = plugin.getDatabase().getProtectedPlayerDao();
        this.premiumPlayers = new PremiumPlayerCache();
        this.premiumCache = new PremiumUserCache();
    }

    @Override
    public void onLogin(@Nullable ServerPlayer player) {
        if (player == null) return;

        final Config config = plugin.getConfig();
        final InetAddress address = player.getInetAddress();

        if (address == null || isAuthenticating(player)) {
            player.disconnect(config.getMessages().getInternalError());
            return;
        }

        logger.debug("Check player(" + player.getName() + ") nickname matcher(" + config.getNicknameRegex() + "). Success: " + config.getNicknameRegex().matcher(player.getName()).matches());

        if (!config.getNicknameRegex().matcher(player.getName()).matches()) {
            player.disconnect(config.getMessages().getInvalidCharacterInName());
            return;
        }

        getProtectedPlayer(player.getUUID()).thenCompose(protectedPlayer -> {
            if (protectedPlayer == null) {
                logger.debug("Create ProtectedPlayer. Username: " + player.getName());
                return createProtectedPlayer(player.getLowercaseName(), player.getName(), null, 0, System.currentTimeMillis(), address.getHostAddress(), address.getHostAddress(), player.getUUID());
            }
            return CompletableFuture.completedFuture(protectedPlayer);
        }).thenAccept(protectedPlayer -> {
            try {
                if (protectedPlayer == null) {
                    player.disconnect(config.getMessages().getInternalError());
                    return;
                }

                if (protectedPlayer.isBanned()) {
                    player.disconnect(config.getMessages().getAccountBanned());
                    return;
                }

                if (config.isNameCaseControl() && !protectedPlayer.getRealName().equals(player.getName())) {
                    player.disconnect(config.getMessages().getNameCaseFailed()
                            .replace("%current_name%", player.getName())
                            .replace("%need_name%", protectedPlayer.getRealName()));
                    return;
                }

                if (!protectedPlayer.isRegister()) {
                    logger.debug("The player(" + player.getName() + ") has started registration");

                    authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
                    new RegisterRunnable(plugin, player);
                    this.connectPlayer(player, config.findServer(config.getAuthServers()));
                    plugin.getSongManager().play(player, plugin.getSongManager().findRandomSong());
                    return;
                }

                PremiumPlayer premiumPlayer = getOnlinePremiumPlayer(player.getLowercaseName());
                if (isPremium(player.getLowercaseName())) {
                    logger.debug("PremiumPlayer found: %s".formatted(premiumPlayer));
                    if (premiumPlayer != null && premiumPlayer.isValidTemplateTime()) {
                        UUID onlineUuid = getPremiumUuid(protectedPlayer.getLowercaseName());
                        if (onlineUuid != null) {
                            protectedPlayer.setOnlineUuid(onlineUuid);
                        }
                        premiumPlayers.addOrUpdateCache(new PremiumPlayer(premiumPlayer.getLowercaseName(), false, 0));
                        connectGameServer(player);
                    }

                    logger.debug("Automatic login for player(%s) with premium status".formatted(player.getName()));
                    player.sendMessage(config.getMessages().getPremiumAccountAutoLogin());
                    performLogin(protectedPlayer
                            .setLowercaseName(player.getLowercaseName())
                            .setRealName(player.getName()));
                    return;
                }

                logger.debug("Checking the player(" + player.getName() + ") for an active session(" + config.getSessionTime() + ") and a match in the IP-address(" + address.getHostAddress() + ")");

                if (protectedPlayer.isSessionActive(config.getSessionTime()) && protectedPlayer.isValidIp(address.getHostAddress())) {
                    logger.debug("the player(" + player.getName() + ") has an active session and a valid IP-address(" + address.getHostAddress() + ")");

                    player.sendMessage(config.getMessages().getSessionActive());
                    this.connectPlayer(player, config.findServer(config.getLobbyServers()));
                    return;
                }

                logger.debug("The player(" + player.getName() + ") has started authorization");

                authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
                new LoginRunnable(plugin, player);
                connectAuthServer(player);
                plugin.getSongManager().play(player, plugin.getSongManager().findRandomSong());
            } catch (Throwable e) {
                player.disconnect(config.getMessages().getInternalError());
                e.printStackTrace();
            }
        });
    }

    @Override
    public <T> void onPremiumLogin(String playerName, PremiumProvider<T> premiumProvider, T obj) {
        if (playerName == null) return;

        try {
            ProtectedPlayer protectedPlayer = getProtectedPlayer(playerName).get();
            if (protectedPlayer != null) {
                PremiumPlayer player = getOnlinePremiumPlayer(protectedPlayer.getLowercaseName());
                if (isPremium(playerName)) {
                    if (player == null)
                        premiumPlayers.addOrUpdateCache(new PremiumPlayer(playerName, false, 0));

                    premiumProvider.changeOfflineMode(obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect(ServerPlayer player) {
        getAuthPlayers().remove(player.getLowercaseName());

        ConfirmationPlayer confirmationPlayer = plugin.getSocialManager().getConfirmationByName(player.getLowercaseName());
        plugin.getSocialManager().getConfirmationPlayers().remove(confirmationPlayer);
    }

    @Override
    public void onAzLinkRegister(String name, UUID uuid, String password, InetAddress address) {
        try {
            final Config config = plugin.getConfig();

            if (config.isAzLinkIntegration() && azLinkPlatform != null) {
                azLinkPlatform.getPlugin().getHttpClient().registerUser(name, null, uuid, password, address).exceptionally(e -> {
                    plugin.getServerLogger().debug("Unable to register " + name + " - " + e.getMessage());
                    return null;
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAzLinkChangePassword(String name, UUID uuid, String password) {
        try {
            final Config config = plugin.getConfig();

            if (config.isAzLinkIntegration() && azLinkPlatform != null) {
                azLinkPlatform.getPlugin().getHttpClient().updatePassword(uuid, password).exceptionally(e -> {
                    plugin.getServerLogger().debug("Unable to change password " + name + " - " + e.getMessage());
                    return null;
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
    public boolean isAuthenticating(ServerPlayer player) {
        final SocialManager socialManager = plugin.getSocialManager();
        return (getAuthPlayers().containsKey(player.getLowercaseName())
                || socialManager.getConfirmationByName(player.getLowercaseName()) != null);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> createProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, String registerIp, String lastIp, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer execute = new ProtectedPlayer(lowercaseName, realName, uuid, null, password, session, lastJoin, false, true, true, true, registerIp, lastIp, 0, 0, 0);
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
                ProtectedPlayer cachedData = playerCache.getCaches().stream()
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
                ProtectedPlayer cachedData = playerCache.getCaches().stream()
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
    @SneakyThrows
    public boolean isPremium(String playerName) {
        UUID onlineUuid = getPremiumUuid(playerName);
        UUID uuid = UuidGenerator.byName(playerName);
        logger.debug("Searching for online UUID(%s) and generating offline UUID(%s) for player(%s)".formatted(onlineUuid, uuid, playerName));
        try {
            PremiumPlayer premiumPlayer = getOnlinePremiumPlayer(playerName.toLowerCase());
            return (premiumPlayer != null && premiumPlayer.isValidTemplateTime())
                    || (getProtectedPlayer(uuid).thenApply(p -> onlineUuid != null && onlineUuid.equals(p.getOnlineUuid())).get());
        } catch (Exception e) {
            logger.debug("The BaseAuthManager#isPremium method throws an exception. Message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public UUID getPremiumUuid(String playerName) {
        final Config config = plugin.getConfig();

        try {
            PremiumUser data = premiumCache.getCacheData(playerName);
            if (data != null) {
                return data.getUuid();
            }

            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                String id = json.get("id").getAsString();
                UUID uuid = UUID.fromString(id.replaceFirst(
                        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5"
                ));
                data = new PremiumUser(playerName, uuid, System.currentTimeMillis() + config.getPremiumCacheTimeUnit().toMillis(config.getPremiumCacheTimeUnitValue()));
                premiumCache.addOrUpdateCache(data);
                return uuid;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    @Override
    public Map<String, Integer> getAuthPlayers() {
        return authorizationPlayers;
    }

    public PremiumPlayer getOnlinePremiumPlayer(String lowercaseName) {
        return premiumPlayers.stream()
                .filter(p -> p.getLowercaseName().equals(lowercaseName))
                .findFirst()
                .orElse(null);
    }

    public Cache<PremiumPlayer, String> getPremiumPlayerCache() {
        return premiumPlayers;
    }
}
