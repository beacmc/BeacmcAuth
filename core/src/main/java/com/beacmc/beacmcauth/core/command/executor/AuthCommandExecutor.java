package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import org.mindrot.jbcrypt.BCrypt;

import org.jetbrains.annotations.Nullable;
import java.sql.SQLException;

public class AuthCommandExecutor implements CommandExecutor {

    private final ProtectedPlayerDao dao;
    private final Database database;
    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public AuthCommandExecutor(BeacmcAuth plugin) {
        this.database = plugin.getDatabase();
        this.dao = database.getProtectedPlayerDao();
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

        sender.sendMessage(config.getMessages().getAuthHelp());
    }

    private void handleReloadConfig(CommandSender sender, String[] ignored) {
        final Config config = plugin.getConfig();

        sender.sendMessage(config.getMessages().getAuthReload());
        plugin.reloadAllConfigurations();
    }

    private void handleDeleteAccount(CommandSender sender, String[] args) {
        final Config config = plugin.getConfig();

        authManager.getProtectedPlayer(args[1].toLowerCase()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                sender.sendMessage(config.getMessages().getAccountNotFound());
                return;
            }

            @Nullable ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            try {
                if (player != null) {
                    player.disconnect(config.getMessages().getYourAccountDeletedDisconnect());
                }
                dao.delete(protectedPlayer);
                database.getPlayersCache().removeCache(protectedPlayer);
                sender.sendMessage(config.getMessages().getAccountDeleted());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleChangePassword(CommandSender sender, String[] args) {
        final Config config = plugin.getConfig();

        authManager.getProtectedPlayer(args[1].toLowerCase()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                sender.sendMessage(config.getMessages().getAccountNotFound());
                return;
            }

            if (!protectedPlayer.isRegister()) {
                sender.sendMessage(config.getMessages().getAccountNotFound());
                return;
            }

            try {
                dao.createOrUpdate(protectedPlayer.setSession(0).setPassword(BCrypt.hashpw(args[2], BCrypt.gensalt(config.getBCryptRounds()))));
                database.getPlayersCache().addOrUpdateCache(protectedPlayer);
                authManager.onAzLinkChangePassword(protectedPlayer.getRealName(), protectedPlayer.getUuid(), args[2]);
                sender.sendMessage(config.getMessages().getAccountPasswordChanged());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
