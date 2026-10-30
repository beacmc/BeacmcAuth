package com.beacmc.beacmcauth.core.auth.protection;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.action.AuthActionType;
import com.beacmc.beacmcauth.api.auth.protection.AntiBruteForceManager;
import com.beacmc.beacmcauth.api.auth.protection.AwaitingDangerousCommand;
import com.beacmc.beacmcauth.api.auth.protection.ProtectionManager;
import com.beacmc.beacmcauth.api.auth.protection.attack.AttackStatusProvider;
import com.beacmc.beacmcauth.api.config.DangerousCommandConfirmationSettings;
import com.beacmc.beacmcauth.api.config.ProtectionConfig;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class BaseProtectionManager implements ProtectionManager {

    private final List<AwaitingDangerousCommand> awaitingDangerousCommands
            = Collections.synchronizedList(new ArrayList<>());
    private final BeacmcAuth plugin;
    private @Nullable AttackStatusProvider attackStatusProvider;
    private AntiBruteForceManager antiBruteForceManager;

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
        return isBlockedAction(player.getName(), actionType);
    }

    @Override
    public boolean isBlockedAction(String playerName, AuthActionType actionType) {
        ProtectionConfig config = plugin.getProtectionConfig();
        return isBlockedAction(actionType) && !config.isExemptPlayer(playerName);
    }

    @Override
    public boolean isAwaitingDangerousCommandConfirmation(UUID playerId, String command) {
        synchronized (awaitingDangerousCommands) {
            return awaitingDangerousCommands.stream()
                    .anyMatch(awaiting -> awaiting.uuid().equals(playerId)
                            && awaiting.command().equals(command));
        }
    }

    @Override
    public void createAwaitingDangerousCommandConfirmation(UUID playerId, String command) {
        DangerousCommandConfirmationSettings settings = plugin.getProtectionConfig()
                .getDangerousCommandConfirmationSettings();

        awaitingDangerousCommands.add(new AwaitingDangerousCommand(
                playerId,
                command,
                settings.getConfirmationLifetimeMillis()
        ));
    }

    @Override
    public AntiBruteForceManager getAntiBruteForceManager() {
        if (antiBruteForceManager == null) {
            antiBruteForceManager = new BaseAntiBruteForceManager(plugin);
        }
        return antiBruteForceManager;
    }

    @Override
    public void clearPlayerCache(ServerPlayer player) {
        synchronized (awaitingDangerousCommands) {
            awaitingDangerousCommands.removeIf(awaiting ->
                    awaiting.uuid().equals(player.getUUID()));
        }
    }
}
