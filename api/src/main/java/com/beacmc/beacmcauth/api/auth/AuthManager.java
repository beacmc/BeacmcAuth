package com.beacmc.beacmcauth.api.auth;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.auth.premium.PremiumProvider;
import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;

import java.awt.desktop.AboutEvent;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface AuthManager {

    void onLogin(ServerPlayer player);

    <T> void onPremiumLogin(String playerName, PremiumProvider<T> premiumProvider, T obj);

    void onDisconnect(ServerPlayer player);

    void onAzLinkRegister(String name, UUID uuid, String password, InetAddress address);

    void onAzLinkChangePassword(String name, UUID uuid, String password);

    void connectPlayer(ServerPlayer player, Server server);

    void connectAuthServer(ServerPlayer player);

    void connectGameServer(ServerPlayer player);

    boolean isAuthenticating(ServerPlayer player);

    CompletableFuture<ProtectedPlayer> createProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, String registerIp, String lastIp, UUID uuid);

    CompletableFuture<ProtectedPlayer> getProtectedPlayer(UUID uuid);

    CompletableFuture<ProtectedPlayer> getProtectedPlayer(String playerName);

    CompletableFuture<ProtectedPlayer> getPremiumPlayer(UUID premiumUuid);

    CompletableFuture<ProtectedPlayer> performLogin(ProtectedPlayer protectedPlayer);

    CompletableFuture<ProtectedPlayer> register(ProtectedPlayer protectedPlayer, String password);

    Map<String, Integer> getAuthPlayers();

    boolean isPremium(String playerName);

    UUID getPremiumUuid(String playerName);

    Cache<PremiumUser, String> getPremiumCache();

    Cache<PremiumPlayer, String> getPremiumPlayerCache();
}
