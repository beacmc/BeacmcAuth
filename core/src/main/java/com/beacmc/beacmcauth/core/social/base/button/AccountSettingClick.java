package com.beacmc.beacmcauth.core.social.base.button;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.config.social.SocialMessages;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickListener;
import com.beacmc.beacmcauth.core.util.CodeGenerator;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AccountSettingClick implements ButtonClickListener {

    private final BeacmcAuth plugin;
    private final ExecutorService executorService;

    public AccountSettingClick(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.executorService = plugin.getExecutorService();
    }

    @Override
    public void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, Button button) {
        String[] args = button.getCallbackData().split(":");
        if (args.length < 2) return;

        String id = args[1];

        switch (args[0]) {
            case "toggle-2fa" -> handleTwoFA(socialPlayer, social, id);
            case "kick" -> handleKick(socialPlayer, social, id);
            case "toggle-ban" -> handleBan(socialPlayer, social, id);
            case "unlink" -> handleUnlink(socialPlayer, social, id);
            case "reset-password" -> handleResetPassword(socialPlayer, social, id);
        }
    }

    private void handleKick(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final Config config = plugin.getConfig();
        final ConfigMessages messages = config.getMessages();
        final SocialMessages socialMessages = socialConfig.getMessages();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {;
            if (protectedPlayer == null || !socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialMessages.getAccountNotLinked());
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (player == null) {
                socialPlayer.sendPrivateMessage(socialMessages.getPlayerOffline());
                return;
            }

            socialPlayer.sendPrivateMessage(socialMessages.getAccountKickSuccess());

            String disconnectMessage = switch (social.getType()) {
                case DISCORD -> messages.getDiscordKick();
                case TELEGRAM -> messages.getTelegramKick();
                case CUSTOM -> null;
            };
            player.disconnect(disconnectMessage);
        });
    }

    private void handleTwoFA(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final SocialMessages messages = socialConfig.getMessages();

        if (social.isCooldown(socialPlayer.getID())) {
            socialPlayer.sendPrivateMessage(messages.getCooldown());
            return;
        }
        social.createCooldown(socialPlayer.getID(), 500);

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null || !socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(messages.getAccountNotLinked());
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                social.switchPlayerTwoFa(protectedPlayer, !social.isPlayerTwoFaEnabled(protectedPlayer));

                if (social.isPlayerTwoFaEnabled(protectedPlayer)) {
                    socialPlayer.sendPrivateMessage(messages.getAccount2faEnabled());
                } else {
                    socialPlayer.sendPrivateMessage(messages.getAccount2faDisabled());
                }

                return protectedPlayer;
            }, executorService);
        });
    }

    private void handleBan(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final Config config = plugin.getConfig();
        final SocialMessages messages = socialConfig.getMessages();

        if (social.isCooldown(socialPlayer.getID())) {
            socialPlayer.sendPrivateMessage(messages.getCooldown());
            return;
        }
        social.createCooldown(socialPlayer.getID(), 500);

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null || !socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(messages.getAccountNotLinked());
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setBanned(!protectedPlayer.isBanned()).setSession(0);
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                if (protectedPlayer.isBanned()) {
                    socialPlayer.sendPrivateMessage(messages.getAccountBanned());
                    if (player != null) {
                        player.disconnect(config.getMessages().getAccountBanned());
                    }
                } else {
                    socialPlayer.sendPrivateMessage(messages.getAccountUnbanned());
                }

                return protectedPlayer;
            }, executorService);
        });
    }

    private void handleResetPassword(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final AuthManager authManager = plugin.getAuthManager();
        final SocialMessages messages = socialConfig.getMessages();

        if (social.isCooldown(socialPlayer.getID())) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getCooldown());
            return;
        }
        social.createCooldown(socialPlayer.getID(), 500);

        authManager.getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null || !socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(messages.getAccountNotLinked());
                return;
            }

            String password = CodeGenerator.generate(socialConfig.getResetPasswordChars(), socialConfig.getPasswordResetLength());
            authManager.changePassword(protectedPlayer, password);
            socialPlayer.sendPrivateMessage(messages.getAccountResetPassword()
                    .replace("%name%", protectedPlayer.getRealName())
                    .replace("%password%", password));
        });
    }

    private void handleUnlink(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final SocialMessages messages = socialConfig.getMessages();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null || !socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(messages.getAccountNotLinked());
                return;
            }

            if (socialConfig.isDisableUnlink()) {
                socialPlayer.sendPrivateMessage(messages.getUnlinkDisabled());
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                social.unlinkPlayer(protectedPlayer);
                socialPlayer.sendPrivateMessage(messages.getAccountUnlinkSuccess());
                return protectedPlayer;
            }, executorService);
        });
    }
}
