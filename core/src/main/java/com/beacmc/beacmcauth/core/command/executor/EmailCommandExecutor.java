package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.config.EmailConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.email.EmailManager;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;
import com.beacmc.beacmcauth.core.cache.cooldown.RecoveryCooldown;
import com.beacmc.beacmcauth.core.util.CodeGenerator;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class EmailCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;
    private final EmailManager emailManager;
    private final ExecutorService executorService;
    private final Cache<ProtectedPlayer, UUID> cache;
    private final ProtectedPlayerDao dao;
    private final RecoveryCooldown cooldown;
    private final GameCooldown gameCooldown;

    public EmailCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
        this.emailManager = plugin.getEmailManager();
        this.executorService = plugin.getExecutorService();
        this.cache = authManager.getPlayerCache();
        this.dao = plugin.getDatabase().getProtectedPlayerDao();
        this.cooldown = RecoveryCooldown.getInstance();
        this.gameCooldown = GameCooldown.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) return;

        final EmailConfig emailConfig = plugin.getEmailConfig();
        final Config config = plugin.getConfig();
        final ConfigMessages messages = config.getMessages();

        if (gameCooldown.isCooldown(player.getLowercaseName())) {
            player.sendMessage(config.getMessages().getCooldown());
            return;
        }
        gameCooldown.createCooldown(player.getLowercaseName(), 1_000);

        if (args.length < 1) {
            player.sendMessage(messages.getEmailCommandUsage());
            return;
        }

        authManager.getProtectedPlayer(player.getUUID()).thenAcceptAsync(protectedPlayer -> {
            if (protectedPlayer == null) {
                return;
            }

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    if (authManager.isAuthenticating(player)) return;

                    if (args.length < 2) {
                        player.sendMessage(messages.getEmailAddCommandUsage());
                        return;
                    }

                    if (protectedPlayer.getEmail() != null) {
                        player.sendMessage(messages.getEmailNotAdded());
                        return;
                    }

                    String email = args[1];
                    if (!emailManager.isEmail(email)) {
                        player.sendMessage(messages.getEmailInvalid());
                        return;
                    }

                    try {
                        dao.createOrUpdate(protectedPlayer.setEmail(email));
                        cache.addOrUpdateCache(protectedPlayer);
                        player.sendMessage(messages.getEmailAdded());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                case "recovery" -> {
                    if (!authManager.isAuthenticating(player)) {
                        player.sendMessage(messages.getAlreadyAuthed());
                        return;
                    }

                    if (cooldown.isCooldown(player.getUUID())) {
                        player.sendMessage(messages.getCooldown());
                        return;
                    }

                    if (args.length < 2) {
                        player.sendMessage(messages.getEmailRecoveryCommandUsage());
                        return;
                    }

                    if (protectedPlayer.getEmail() == null) {
                        player.sendMessage(messages.getEmailNotAdded());
                        return;
                    }

                    String email = args[1];
                    if (!emailManager.isEmail(email)) {
                        player.sendMessage(messages.getEmailInvalid());
                        return;
                    }

                    if (!email.equals(protectedPlayer.getEmail())) {
                        player.sendMessage(messages.getEmailRecoveryInvalidDisconnect());
                        return;
                    }

                    String newPassword = CodeGenerator.generate(config.getRecoveryPasswordChars(), 8);
                    emailManager.sendMailMessage(email, Map.of(
                            "%player%", player.getName(),
                            "%password%", newPassword)).thenAccept(result -> {
                        try {
                            if (result) {
                                cooldown.createCooldown(player.getUUID(), emailConfig.getRecoveryCooldownMillis());
                                dao.createOrUpdate(protectedPlayer.setPassword(
                                        BCrypt.hashpw(newPassword,
                                                BCrypt.gensalt(config.getBCryptRounds()))
                                ).setSession(0));
                                cache.addOrUpdateCache(protectedPlayer);
                                player.disconnect(messages.getEmailRecoveryDisconnect());
                                return;
                            }

                            player.sendMessage(messages.getCouldNotSendEmailMessages());

                        } catch (SQLException e) {
                            throw new CompletionException(e);
                        }
                    }).exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
                }
                case "remove" -> {
                    if (authManager.isAuthenticating(player)) return;

                    if (protectedPlayer.getEmail() == null) {
                        player.sendMessage(messages.getEmailNotAdded());
                        return;
                    }

                    try {
                        dao.createOrUpdate(protectedPlayer.setEmail(null));
                        cache.addOrUpdateCache(protectedPlayer);
                        player.sendMessage(messages.getEmailRemoved());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, executorService);
    }
}
