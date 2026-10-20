package com.beacmc.beacmcauth.api.config;

import com.beacmc.beacmcauth.api.auth.action.AuthActionType;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface ProtectionConfig {

    Map<String, Boolean> getBlockedActionsByDefault();

    Map<String, Boolean> getBlockedActionsOnAttack();

    List<String> getExemptPlayers();

    default boolean isBlockedByDefault(@NotNull AuthActionType actionType) {
        return getBlockedActionsByDefault().getOrDefault(actionType.getIdentifier(), false);
    }

    default boolean isBlockedIfAttack(@NotNull AuthActionType actionType) {
        return getBlockedActionsByDefault().getOrDefault(actionType.getIdentifier(), false);
    }

    default boolean isExemptPlayer(String exemptPlayer) {
        return getExemptPlayers().stream()
                .anyMatch(exemptPlayer::equalsIgnoreCase);
    }

    default boolean isExemptPlayer(ServerPlayer exemptPlayer) {
        return isExemptPlayer(exemptPlayer.getName());
    }
}
