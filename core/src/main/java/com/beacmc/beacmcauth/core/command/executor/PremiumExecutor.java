package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;
import java.util.UUID;

public class PremiumExecutor implements CommandExecutor {

    private final AuthManager authManager;
    private final BeacmcAuth plugin;
    private final GameCooldown cooldown;

    public PremiumExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;

        authManager = plugin.getAuthManager();
        cooldown = GameCooldown.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) return;

        final Config config = plugin.getConfig();
        final ConfigMessages messages = config.getMessages();

        authManager.getProtectedPlayer(player.getUUID()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) return;

            if (protectedPlayer.getOnlineUuid() != null) {
                player.sendMessage(messages.getAlreadyPremium());
                return;
            }

            UUID uuid = authManager.getPremiumUuid(player.getLowercaseName());
            if (uuid == null) {
                player.sendMessage(messages.getPremiumAccountNotFound());
                return;
            }

            authManager.getPremiumPlayerCache().addCache(new PremiumPlayer(
                    protectedPlayer.getLowercaseName(),
                    true,
                    System.currentTimeMillis() + config.getLifetimeOfTemporaryPremiumVerificationTimeUnit().toMillis(config.getLifetimeOfTemporaryPremiumVerificationTimeUnitValue())
            ));
            player.disconnect(messages.getPremiumSuccess());
        });
    }
}
