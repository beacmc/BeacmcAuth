package com.beacmc.beacmcauth.core.social.base.button;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.button.ButtonClickListener;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.core.util.CodeGenerator;
import lombok.AllArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class AccountSettingClick implements ButtonClickListener {

    private final BeacmcAuth plugin;

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

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (!socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            if (player == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("player-offline"));
                return;
            }

            socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-kick-success"));
            player.disconnect(config.getMessage(social.getGameConfigPrefixMessage() + "kick"));
        });
    }

    private void handleTwoFA(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final AuthManager authManager = plugin.getAuthManager();

        if (social.isCooldown(socialPlayer.getID())) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessage("cooldown"));
            return;
        }
        social.createCooldown(socialPlayer.getID(), 6000L);

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            if (!socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    social.switchPlayerTwoFa(protectedPlayer, !social.isPlayerTwoFaEnabled(protectedPlayer));
                    dao.createOrUpdate(protectedPlayer);
                    authManager.getPlayerCache().addOrUpdateCache(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (social.isPlayerTwoFaEnabled(protectedPlayer)) {
                    socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-2fa-enabled"));
                } else {
                    socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-2fa-disabled"));
                }

                return protectedPlayer;
            });
        });
    }

    private void handleBan(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final Config config = plugin.getConfig();
        final AuthManager authManager = plugin.getAuthManager();

        if (social.isCooldown(socialPlayer.getID())) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessage("cooldown"));
            return;
        }
        social.createCooldown(socialPlayer.getID(), 6000L);

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            if (!socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setBanned(!protectedPlayer.isBanned()).setSession(0);
                    dao.createOrUpdate(protectedPlayer);
                    authManager.getPlayerCache().addOrUpdateCache(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (protectedPlayer.isBanned()) {
                    socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-banned"));
                    if (player != null) {
                        player.disconnect(config.getMessage("account-banned"));
                    }
                } else {
                    socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-unbanned"));
                }

                return protectedPlayer;
            });
        });
    }

    private void handleResetPassword(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final Config config = plugin.getConfig();
        final AuthManager authManager = plugin.getAuthManager();

        if (social.isCooldown(socialPlayer.getID())) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessage("cooldown"));
            return;
        }
        social.createCooldown(socialPlayer.getID(), 6000L);

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            if (!socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                String password = CodeGenerator.generate(socialConfig.getResetPasswordChars(), socialConfig.getPasswordResetLength());
                try {
                    protectedPlayer.setSession(0).setPassword(BCrypt.hashpw(password, BCrypt.gensalt(config.getBCryptRounds())));
                    dao.createOrUpdate(protectedPlayer);
                    authManager.getPlayerCache().addOrUpdateCache(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-reset-password", Map.of("%name%", protectedPlayer.getRealName(), "%password%", password)));
                return protectedPlayer;
            });
        });
    }

    private void handleUnlink(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String id) {
        final SocialConfig socialConfig = social.getSocialConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            if (!socialPlayer.checkAccountLink(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-not-linked"));
                return;
            }

            if (socialConfig.isDisableUnlink()) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("unlink-disabled"));;
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                social.unlinkPlayer(protectedPlayer);
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("account-unlink-success"));
                return protectedPlayer;
            });
        });
    }
}
