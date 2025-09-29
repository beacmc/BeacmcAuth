package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;

import java.sql.SQLException;
import java.util.UUID;

public class CrackExecutor implements CommandExecutor {

    private final AuthManager authManager;
    private final BeacmcAuth plugin;
    private final GameCooldown cooldown;

    public CrackExecutor(BeacmcAuth plugin) {
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
            try {
                if (protectedPlayer == null) return;

                if (protectedPlayer.getOnlineUuid() == null) {
                    player.sendMessage(messages.getAlreadyCrack());
                    return;
                }

                plugin.getDatabase().getProtectedPlayerDao().createOrUpdate(protectedPlayer
                        .setOnlineUuid(null)
                        .setSession(0));
                PremiumPlayer premiumPlayer = authManager.getPremiumPlayerCache().getCacheData(protectedPlayer.getLowercaseName());
                if (premiumPlayer != null) {
                    authManager.getPremiumPlayerCache().removeCache(premiumPlayer);
                }
                player.disconnect(messages.getCrackSuccess());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
