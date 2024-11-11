package com.beacmc.beacmcauth.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthCommand extends Command {

    private final ProtectedPlayerDao dao;

    public AuthCommand() {
        super("beacmcauth", "beacmcauth.admin", "auth");
        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        final BaseConfig config = BeacmcAuth.getConfig();

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

    private void handleReloadConfig(CommandSender sender, String[] args) {
        final BaseConfig config = BeacmcAuth.getConfig();

        sender.sendMessage(config.getMessage("auth-reload"));
        BeacmcAuth.getInstance().reloadConfigs();
    }

    private void handleDeleteAccount(CommandSender sender, String[] args) {
        final BaseConfig config = BeacmcAuth.getConfig();

        ProtectedPlayer.get(args[1].toLowerCase()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                sender.sendMessage(config.getMessage("account-not-found"));
                return;
            }

            try {
                if (protectedPlayer.getPlayer() != null) {
                    protectedPlayer.getPlayer().disconnect(config.getMessage("your-account-deleted-disconnect"));
                }
                dao.delete(protectedPlayer);
                sender.sendMessage(config.getMessage("account-deleted"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleChangePassword(CommandSender sender, String[] args) {
        final BaseConfig config = BeacmcAuth.getConfig();

        ProtectedPlayer.get(args[1].toLowerCase()).thenAccept(protectedPlayer -> {
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
                sender.sendMessage(config.getMessage("account-password-changed"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
