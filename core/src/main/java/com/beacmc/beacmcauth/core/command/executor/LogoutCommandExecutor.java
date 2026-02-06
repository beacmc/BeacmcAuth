package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class LogoutCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) return;

        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final Config config = plugin.getConfig();
        final ConfigMessages messages = config.getMessages();
        final GameCooldown cooldown = GameCooldown.getInstance();

        if (cooldown.isCooldown(player.getLowercaseName())) {
            player.sendMessage(messages.getCooldown());
            return;
        }

        if (authManager.isAuthenticating(player)) {
            player.sendMessage(messages.getNotAuthed());
            return;
        }

        authManager.getProtectedPlayer(player.getUUID()).thenAcceptAsync(protectedPlayer -> {
            if (protectedPlayer == null) return;

            try {
                dao.createOrUpdate(protectedPlayer.setSession(0));
                authManager.getPlayerCache().addOrUpdateCache(protectedPlayer);
                player.disconnect(messages.getLogoutDisconnect());
                cooldown.createCooldown(player.getLowercaseName(), 5_000);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, plugin.getExecutorService());
    }
}
