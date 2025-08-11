package com.beacmc.beacmcauth.core.auth;

import com.azuriom.azlink.common.AzLinkPlatform;
import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.core.cache.PlayerCache;
import com.beacmc.beacmcauth.core.util.runnable.LoginRunnable;
import com.beacmc.beacmcauth.core.util.runnable.RegisterRunnable;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseAuthManager implements AuthManager {

    private final BeacmcAuth plugin;
    private final Map<String, Integer> authorizationPlayers;
    private final ProtectedPlayerDao dao;
    private final ServerLogger logger;
    private final AzLinkPlatform azLinkPlatform;
    private final Cache<ProtectedPlayer, String> playerCache;
    private final ExecutorService executorService;

    public BaseAuthManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.executorService = plugin.getExecutorService();
        this.playerCache = new PlayerCache();
        this.authorizationPlayers = new HashMap<>();
        this.logger = plugin.getServerLogger();
        this.azLinkPlatform = plugin.getProxy().getPlugin("AzLink");
        this.dao = plugin.getDatabase().getProtectedPlayerDao();
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

        getProtectedPlayer(player.getLowercaseName()).thenCompose(protectedPlayer -> {
            if (protectedPlayer == null) {
                return createProtectedPlayer(player.getLowercaseName(), player.getName(), null, 0, System.currentTimeMillis(), address.getHostAddress(), address.getHostAddress(), player.getUUID());
            }
            return CompletableFuture.completedFuture(protectedPlayer);
        }).thenAccept(protectedPlayer -> {
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
            this.connectPlayer(player, config.findServer(config.getAuthServers()));
        });
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
                ProtectedPlayer execute = new ProtectedPlayer(lowercaseName, realName, uuid, password, session, lastJoin, false, true, true, true, registerIp, lastIp, 0, 0, 0);
                dao.create(execute);
                return execute;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executorService);
    }

    @Override
    public CompletableFuture<ProtectedPlayer> getProtectedPlayer(String lowercaseName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer cacheData = playerCache.getCacheData(lowercaseName);
                if (cacheData == null) {
                    ProtectedPlayer requestData = dao.queryForId(lowercaseName);
                    playerCache.addOrUpdateCache(requestData);
                    return requestData;
                }
                return cacheData;
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
                dao.createOrUpdate(protectedPlayer.setLastIp(ip).setSession(currentTime).setLastJoin(currentTime));
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
    public Map<String, Integer> getAuthPlayers() {
        return authorizationPlayers;
    }

    @Override
    public Cache<ProtectedPlayer, String> getPlayerCache() {
        return playerCache;
    }
}
