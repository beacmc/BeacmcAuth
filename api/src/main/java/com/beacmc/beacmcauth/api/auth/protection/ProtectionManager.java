package com.beacmc.beacmcauth.api.auth.protection;

import com.beacmc.beacmcauth.api.auth.action.AuthActionType;
import com.beacmc.beacmcauth.api.auth.protection.attack.AttackStatusProvider;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ProtectionManager {

    @Nullable AttackStatusProvider getAttackStatusProvider();

    ProtectionManager setAttackStatusProvider(AttackStatusProvider provider);

    boolean isBlockedAction(AuthActionType actionType);

    boolean isBlockedAction(ServerPlayer player, AuthActionType actionType);

    boolean isBlockedAction(String playerName, AuthActionType actionType);

    default boolean isAwaitingDangerousCommandConfirmation(ServerPlayer player, String command) {
        return isAwaitingDangerousCommandConfirmation(player.getUUID(), command);
    }

    default boolean isAwaitingDangerousCommandConfirmation(ProtectedPlayer protectedPlayer, String command) {
        return isAwaitingDangerousCommandConfirmation(protectedPlayer.getUuid(), command);
    }

    boolean isAwaitingDangerousCommandConfirmation(UUID playerId, String command);

    default void createAwaitingDangerousCommandConfirmation(ProtectedPlayer protectedPlayer, String command) {
        createAwaitingDangerousCommandConfirmation(protectedPlayer.getUuid(), command);
    }

    default void createAwaitingDangerousCommandConfirmation(ServerPlayer player, String command) {
        createAwaitingDangerousCommandConfirmation(player.getUUID(), command);
    }

    void createAwaitingDangerousCommandConfirmation(UUID playerId, String command);

    void clearPlayerCache(ServerPlayer player);
}
