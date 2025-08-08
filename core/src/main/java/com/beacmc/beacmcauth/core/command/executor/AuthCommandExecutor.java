package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.Nullable;
import java.sql.SQLException;

public class AuthCommandExecutor implements CommandExecutor {

    private final ProtectedPlayerDao dao;
    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public AuthCommandExecutor(BeacmcAuth plugin) {
        dao = plugin.getDatabase().getProtectedPlayerDao();
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        final Config config = plugin.getConfig();

        if (!sender.hasPermission("beacmcauth.admin")) return;

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            handleReloadConfig(sender, args);
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("delete")) {
            handleDeleteAccount(sender, args);
            return;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("changepass")) {
            handleChangePassword(sender, args);
            return;
        }

        sender.sendMessage(config.getMessage("auth-help"));
    }

    private void handleReloadConfig(CommandSender sender, String[] ignored) {
        final Config config = plugin.getConfig();

        sender.sendMessage(config.getMessage("auth-reload"));
        plugin.reloadAllConfigurations();
    }

    private void handleDeleteAccount(CommandSender sender, String[] args) {
        final Config config = plugin.getConfig();

        authManager.getProtectedPlayer(args[1].toLowerCase()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                sender.sendMessage(config.getMessage("account-not-found"));
                return;
            }

            @Nullable ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            try {
                if (player != null) {
                    player.disconnect(config.getMessage("your-account-deleted-disconnect"));
                }
                dao.delete(protectedPlayer);
                authManager.getPlayerCache().removeCache(protectedPlayer);
                sender.sendMessage(config.getMessage("account-deleted"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleChangePassword(CommandSender sender, String[] args) {
        final Config config = plugin.getConfig();

        authManager.getProtectedPlayer(args[1].toLowerCase()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                sender.sendMessage(config.getMessage("account-not-found"));
                return;
            }

            if (!protectedPlayer.isRegister()) {
                sender.sendMessage(config.getMessage("account-not-found"));
                return;
            }

            try {
                dao.createOrUpdate(protectedPlayer.setSession(0).setPassword(BCrypt.hashpw(args[2], BCrypt.gensalt(config.getBCryptRounds()))));
                authManager.getPlayerCache().addOrUpdateCache(protectedPlayer);
                authManager.onAzLinkChangePassword(protectedPlayer.getRealName(), protectedPlayer.getUuid(), args[2]);
                sender.sendMessage(config.getMessage("account-password-changed"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
