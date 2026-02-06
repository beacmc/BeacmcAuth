package com.beacmc.beacmcauth.api.auth.premium.mojang;

import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.auth.premium.response.Response;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface MojangAuthManager {

    CompletableFuture<Response<PremiumUser>> getPremiumUser(String name);

    default CompletableFuture<Response<PremiumUser>> getPremiumUser(@NotNull ServerPlayer player) {
        return getPremiumUser(player.getName());
    }

    default CompletableFuture<Response<PremiumUser>> getPremiumUser(@NotNull ProtectedPlayer player) {
        return getPremiumUser(player.getLowercaseName());
    }

    long getRateLimitTimeExpired();

    default boolean isRateLimited() {
        return getRateLimitTimeExpired() > System.currentTimeMillis();
    }
}
