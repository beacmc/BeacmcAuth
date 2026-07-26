package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.event.type.ChangePasswordEvent;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
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
    private final ServerLogger logger;

    public ChangepasswordCommandExecutor(BeacmcAuth plugin) {
        dao = plugin.getDatabase().getProtectedPlayerDao();
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
        this.cooldown = GameCooldown.getInstance();
        this.logger = plugin.getServerLogger();
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

        authManager.getProtectedPlayer(player.getLowercaseName())
                .thenAccept(protectedPlayer -> {
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

                    assert protectedPlayer.getPassword() != null : "Password cannot be null";
                    String oldPass = protectedPlayer.getPassword();

                    CompletableFuture.supplyAsync(() -> {
                        try {
                            dao.createOrUpdate(protectedPlayer.setPassword(BCrypt.hashpw(args[1], BCrypt.gensalt(config.getBCryptRounds()))));
                            plugin.getEventManager().fire(new ChangePasswordEvent(protectedPlayer, player, oldPass));
                            player.sendMessage(config.getMessages().getChangePasswordSuccess());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }, authManager.getExecutorService());
                }).exceptionally(e -> {
                    logger.error("ChangepasswordCommandExecutor have " + e.getCause().getClass().getSimpleName());
                    logger.error("Message: " + e.getMessage());
                    return null;
                });
    }
}
