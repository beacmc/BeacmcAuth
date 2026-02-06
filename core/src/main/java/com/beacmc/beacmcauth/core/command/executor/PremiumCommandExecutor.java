package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;

public class PremiumCommandExecutor implements CommandExecutor {

    private final AuthManager authManager;
    private final BeacmcAuth plugin;
    private final GameCooldown cooldown;

    public PremiumCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;

        authManager = plugin.getAuthManager();
        cooldown = GameCooldown.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) return;
        if (!player.hasPermission("beacmcauth.premium")) return;

        final Config config = plugin.getConfig();
        final ConfigMessages messages = config.getMessages();

        if (cooldown.isCooldown(player.getLowercaseName())) {
            player.sendMessage(config.getMessages().getCooldown());
            return;
        }
        cooldown.createCooldown(player.getLowercaseName(), 5_000);

        authManager.getProtectedPlayer(player.getUUID()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) return;
            if (protectedPlayer.getOnlineUuid() != null) {
                player.sendMessage(messages.getAlreadyPremium());
                return;
            }

            plugin.getProxy().runTask(() -> {
                PremiumPlayer premiumPlayer = new PremiumPlayer(
                        protectedPlayer.getLowercaseName(),
                        null,
                        true,
                        System.currentTimeMillis() + config.getLifetimeOfTemporaryPremiumVerificationTimeUnit().toMillis(config.getLifetimeOfTemporaryPremiumVerificationTimeUnitValue())
                );

                authManager.getPremiumPlayerCache().addCache(premiumPlayer);
                player.disconnect(messages.getPremiumSuccess());
            });
        });
    }
}
