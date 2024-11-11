package com.beacmc.beacmcauth.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChangepasswordCommand extends Command {

    private final ProtectedPlayerDao dao;

    public ChangepasswordCommand() {
        super("changepassword", null, "changepass", "cp", "cpass");
        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("Only player");
            return;
        }

        final BaseConfig config = BeacmcAuth.getConfig();
        final ProxiedPlayer player = (ProxiedPlayer) sender;
        final Integer minLength = config.getPasswordMinLength();
        final Integer maxLength = config.getPasswordMaxLength();

        if (args.length < 2) {
            player.sendMessage(config.getMessage("change-password-command-usage", Map.of()));
            return;
        }

        CompletableFuture<ProtectedPlayer> future = ProtectedPlayer.get(sender.getName());

        future.thenAccept(protectedPlayer -> {
            if (!protectedPlayer.checkPassword(args[0])) {
                player.sendMessage(config.getMessage("old-password-wrong"));
                return;
            }

            if (args[1].length() < minLength) {
                player.sendMessage(config.getMessage("low-character-password", Map.of()));
                return;
            }

            if (args[1].length() > maxLength) {
                player.sendMessage(config.getMessage("high-character-password", Map.of()));
                return;
            }

            if (args[1].equals(args[0])) {
                player.sendMessage(config.getMessage("passwords-match"));
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    dao.createOrUpdate(protectedPlayer.setPassword(BCrypt.hashpw(args[1], BCrypt.gensalt(config.getBCryptRounds()))));
                    player.sendMessage(config.getMessage("change-password-success"));
                } catch (SQLException e) {
                }
                return null;
            });
        });
    }
}
