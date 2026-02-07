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
import com.beacmc.beacmcauth.core.cache.cooldown.RecoveryCooldown;
import com.beacmc.beacmcauth.core.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Arrays;

@RequiredArgsConstructor
public class SecretCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) return;

        final Config config = plugin.getConfig();
        final GameCooldown gameCooldown = GameCooldown.getInstance();
        final ConfigMessages messages = config.getMessages();
        final RecoveryCooldown cooldown = RecoveryCooldown.getInstance();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();

        if (args.length < 1) {
            player.sendMessage(messages.getSecretCommandUsage());
            return;
        }

        if (gameCooldown.isCooldown(player.getLowercaseName())) {
            player.sendMessage(config.getMessages().getCooldown());
            return;
        }
        gameCooldown.createCooldown(player.getLowercaseName(), 5_000);

        authManager.getProtectedPlayer(player.getUUID()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) return;

            switch (args[0].toLowerCase()) {
                case "set" -> {
                    if (authManager.isAuthenticating(player)) return;

                    if (protectedPlayer.getSecretQuestion() != null) {
                        player.sendMessage(messages.getSecretAnswerAlreadyCreated());
                        return;
                    }

                    if (args.length < 3) {
                        player.sendMessage(messages.getSecretSetCommandUsage());
                        return;
                    }

                    String[] copiedArgs = Arrays.copyOfRange(args, 1, args.length);
                    String joined = String.join(" ", copiedArgs);
                    int index = joined.indexOf('?');

                    if (index == -1 || joined.indexOf('?', index + 1) != -1 || joined.length() > 70) {
                        player.sendMessage(messages.getSecretInvalidSyntax());
                        return;
                    }

                    String question = joined.substring(0, index).trim();
                    String answer = joined.substring(index + 1).trim();
                    if (question.isEmpty() || answer.isEmpty()) {
                        player.sendMessage(messages.getSecretInvalidSyntax());
                        return;
                    }

                    authManager.saveSecretQuestion(protectedPlayer, question, answer);
                    player.sendMessage(messages.getSecretAnswerCreated());
                }
                case "remove" -> {
                    if (authManager.isAuthenticating(player)) return;

                    if (protectedPlayer.getSecretQuestion() == null || protectedPlayer.getHashedSecretAnswer() == null) {
                        player.sendMessage(messages.getSecretAnswerNotCreated());
                        return;
                    }

                    authManager.saveSecretQuestion(protectedPlayer, null, null);
                    player.sendMessage(messages.getSecretAnswerRemoved());
                }
                case "recovery" -> {
                    if (!authManager.isAuthenticating(player)) {
                        player.sendMessage(messages.getAlreadyAuthed());
                        return;
                    }

                    if (protectedPlayer.getSecretQuestion() == null || protectedPlayer.getHashedSecretAnswer() == null) {
                        player.sendMessage(messages.getSecretAnswerNotCreated());
                        return;
                    }

                    if (args.length < 2) {
                        player.sendMessage(messages.getSecretRecovery()
                                .replace("%question%", protectedPlayer.getSecretQuestion()));
                        return;
                    }

                    String answer = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if (answer.length() > 70) {
                        player.sendMessage(messages.getAnswerTooLong());
                        return;
                    }

                    if (!protectedPlayer.checkSecretAnswer(answer)) {
                        player.disconnect(messages.getInvalidSecretAnswerDisconnect());
                        return;
                    }

                    try {

                        cooldown.createCooldown(player.getUUID(), 10_000);

                        String newPassword = CodeGenerator.generate(config.getRecoveryPasswordChars(), 8);
                        dao.createOrUpdate(protectedPlayer.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(config.getBCryptRounds()))));
                        player.disconnect(messages.getSecretAnswerSuccessUsedDisconnect()
                                .replace("%password%", newPassword));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                default -> player.sendMessage(messages.getSecretCommandUsage());
            }
        });
    }
}
