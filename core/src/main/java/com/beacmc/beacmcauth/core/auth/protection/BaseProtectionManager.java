package com.beacmc.beacmcauth.core.auth.protection;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.action.AuthActionType;
import com.beacmc.beacmcauth.api.auth.protection.ProtectionManager;
import com.beacmc.beacmcauth.api.auth.protection.attack.AttackStatusProvider;
import com.beacmc.beacmcauth.api.config.ProtectionConfig;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class BaseProtectionManager implements ProtectionManager {

    private final BeacmcAuth plugin;
    private @Nullable AttackStatusProvider attackStatusProvider;

    @Override
    public boolean isBlockedAction(AuthActionType actionType) {
        ProtectionConfig config = plugin.getProtectionConfig();
        return config.isBlockedByDefault(actionType)
                || (attackStatusProvider != null
                    && attackStatusProvider.isAttacking()
                    && config.isBlockedIfAttack(actionType));
    }

    @Override
    public boolean isBlockedAction(ServerPlayer player, AuthActionType actionType) {
        ProtectionConfig config = plugin.getProtectionConfig();
        return isBlockedAction(actionType) && !config.isExemptPlayer(player);
    }

    @Override
    public boolean isBlockedAction(String playerName, AuthActionType actionType) {
        ProtectionConfig config = plugin.getProtectionConfig();
        return isBlockedAction(actionType) && !config.isExemptPlayer(playerName);
    }
}
