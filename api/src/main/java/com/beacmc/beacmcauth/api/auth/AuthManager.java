package com.beacmc.beacmcauth.api.auth;

import com.beacmc.beacmcauth.api.auth.premium.PremiumChangerProvider;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.crypto.hash.HashedPassword;
import com.beacmc.beacmcauth.api.model.AltAccounts;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Interaction manager of all authorization functions
 */
public interface AuthManager {

    /**
     * Player processing when entering the server
     *
     * @param player - Proxy ServerPlayer
     */
    CompletableFuture<Server> onConnect(ServerPlayer player);

    void onServerConnect(ServerPlayer player);

    <T> CompletableFuture<Void> onPremiumLogin(String playerName, PremiumChangerProvider<T> premiumChangerProvider, T obj);

    void onDisconnect(ServerPlayer player);

    void connectPlayer(ServerPlayer player, Server server);

    void connectAuthServer(ServerPlayer player);

    void connectLobbyServer(ServerPlayer player);

    default boolean isAuthenticating(ServerPlayer player) {
        return isAuthenticating(player.getLowercaseName());
    }

    boolean isAuthenticating(String playerName);

    CompletableFuture<Void> saveSecretQuestion(ProtectedPlayer player, String question, String answer);

    CompletableFuture<ProtectedPlayer> createProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, String registerIp, String lastIp, UUID uuid);

    CompletableFuture<ProtectedPlayer> getProtectedPlayer(UUID uuid);

    CompletableFuture<ProtectedPlayer> getProtectedPlayer(String playerName);

    CompletableFuture<ProtectedPlayer> getProtectedPlayerByOnlineId(UUID onlineId);

    PremiumPlayer getOnlinePremiumPlayer(String name);

    CompletableFuture<ProtectedPlayer> performLogin(ProtectedPlayer protectedPlayer);

    CompletableFuture<ProtectedPlayer> register(ProtectedPlayer protectedPlayer, String password);

    CompletableFuture<ProtectedPlayer> changePassword(ProtectedPlayer protectedPlayer, HashedPassword newPassword);

    CompletableFuture<ProtectedPlayer> changePassword(ProtectedPlayer protectedPlayer, String newPassword);

    CompletableFuture<AltAccounts> getAltAccounts(String hostAddress);

    CompletableFuture<Boolean> checkPassword(ProtectedPlayer player, String hashedPassword);

    void addAltAccount(ProtectedPlayer player, String ip);

    Cache<AuthenticatingPlayer, String> getAuthPlayers();

    Cache<ProtectedPlayer, UUID> getPlayerCache();

    Cache<PremiumPlayer, String> getPremiumPlayerCache();

    ExecutorService getExecutorService();
}
