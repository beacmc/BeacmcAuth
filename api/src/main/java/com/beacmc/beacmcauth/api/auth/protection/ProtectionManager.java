package com.beacmc.beacmcauth.api.auth.protection;

import com.beacmc.beacmcauth.api.auth.action.AuthActionType;
import com.beacmc.beacmcauth.api.auth.protection.attack.AttackStatusProvider;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface ProtectionManager {

    @Nullable AttackStatusProvider getAttackStatusProvider();

    ProtectionManager setAttackStatusProvider(AttackStatusProvider provider);

    boolean isBlockedAction(AuthActionType actionType);

    boolean isBlockedAction(ServerPlayer player, AuthActionType actionType);

    boolean isBlockedAction(String playerName, AuthActionType actionType);
}
