package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ChangepasswordCommandExecutor implements CommandExecutor {

    private final ProtectedPlayerDao dao;
    private final BeacmcAuth plugin;
    private final AuthManager authManager;
    private final GameCooldown cooldown;

    public ChangepasswordCommandExecutor(BeacmcAuth plugin) {
        dao = plugin.getDatabase().getProtectedPlayerDao();
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
        this.cooldown = GameCooldown.getInstance();
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) {
            sender.sendMessage("Only player");
            return;
        }

        final Config config = plugin.getConfig();
        final Pattern passwordPattern = config.getPasswordRegex();

        if (args.length < 2) {
            player.sendMessage(config.getMessages().getChangePasswordCommandUsage());
            return;
        }

        if (cooldown.isCooldown(player.getLowercaseName())) {
            player.sendMessage(config.getMessages().getCooldown());
            return;
        }

        cooldown.createCooldown(player.getLowercaseName(), 5_000);

        CompletableFuture<ProtectedPlayer> future = authManager.getProtectedPlayer(player.getLowercaseName());
        future.thenAccept(protectedPlayer -> {
            if (!protectedPlayer.checkPassword(args[0])) {
                player.sendMessage(config.getMessages().getOldPasswordWrong());
                return;
            }

            if (!passwordPattern.matcher(args[1]).matches()) {
                player.sendMessage(config.getMessages().getInvalidPassword());
                return;
            }

            if (args[1].equals(args[0])) {
                player.sendMessage(config.getMessages().getPasswordsMatch());
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    dao.createOrUpdate(protectedPlayer.setPassword(BCrypt.hashpw(args[1], BCrypt.gensalt(config.getBCryptRounds()))));
                    player.sendMessage(config.getMessages().getChangePasswordSuccess());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }, authManager.getExecutorService());
        });
    }
}
