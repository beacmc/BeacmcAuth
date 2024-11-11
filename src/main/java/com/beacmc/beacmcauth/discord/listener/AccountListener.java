package com.beacmc.beacmcauth.discord.listener;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.util.CodeGenerator;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AccountListener extends ListenerAdapter {

    private final ProtectedPlayerDao dao;

    public AccountListener() {
        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        final String[] split = event.getComponentId().split(":");
        if (split.length != 2) return;

        String action = split[0];
        String id = split[1];

        switch (action.toLowerCase()) {
            case "toggle-2fa": handleTwoFA(event, id); break;
            case "kick": handleKick(event, id); break;
            case "toggle-ban": handleBan(event, id); break;
            case "unlink": handleUnlink(event, id); break;
            case "reset-password": handleResetPassword(event, id); break;
        }
    }

    private void handleKick(ButtonInteractionEvent event, String id) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final BaseConfig config = BeacmcAuth.getConfig();

        ProtectedPlayer.get(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            ProxiedPlayer player = protectedPlayer.getPlayer();

            if (protectedPlayer.getDiscord() != event.getUser().getIdLong()) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            if (player == null) {
                event.reply(discordConfig.getMessage("player-offline")).setEphemeral(true).queue();
                return;
            }

            event.reply(discordConfig.getMessage("account-kick-success")).setEphemeral(true).queue();
            player.disconnect(config.getMessage("discord-kick"));
        });
    }

    private void handleTwoFA(ButtonInteractionEvent event, String id) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();

        ProtectedPlayer.get(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            if (protectedPlayer.getDiscord() != event.getUser().getIdLong()) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setDiscordTwoFaEnabled(!protectedPlayer.isDiscordTwoFaEnabled());
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (protectedPlayer.isDiscordTwoFaEnabled()) {
                    event.reply(discordConfig.getMessage("account-2fa-enabled")).setEphemeral(true).queue();
                } else {
                    event.reply(discordConfig.getMessage("account-2fa-disabled")).setEphemeral(true).queue();
                }

                return protectedPlayer;
            });
        });
    }

    private void handleBan(ButtonInteractionEvent event, String id) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final BaseConfig config = BeacmcAuth.getConfig();

        ProtectedPlayer.get(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            ProxiedPlayer player = protectedPlayer.getPlayer();

            if (protectedPlayer.getDiscord() != event.getUser().getIdLong()) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setBanned(!protectedPlayer.isBanned());
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (protectedPlayer.isBanned()) {
                    event.reply(discordConfig.getMessage("account-banned")).setEphemeral(true).queue();
                    if (player != null) {
                        player.disconnect(config.getMessage("account-banned"));
                    }
                } else {
                    event.reply(discordConfig.getMessage("account-unbanned")).setEphemeral(true).queue();
                }

                return protectedPlayer;
            });
        });
    }

    private void handleResetPassword(ButtonInteractionEvent event, String id) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final BaseConfig config = BeacmcAuth.getConfig();

        ProtectedPlayer.get(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            if (protectedPlayer.getDiscord() != event.getUser().getIdLong()) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                String password = CodeGenerator.generate(discordConfig.getResetPasswordChars(), discordConfig.getPasswordResetLength());
                try {
                    protectedPlayer.setSession(0).setPassword(BCrypt.hashpw(password, BCrypt.gensalt(config.getBCryptRounds())));
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                event.reply(discordConfig.getMessage("account-reset-password", Map.of("%name%", protectedPlayer.getRealName(), "%password%", password)))
                        .setEphemeral(true)
                        .queue();
                return protectedPlayer;
            });
        });
    }

    private void handleUnlink(ButtonInteractionEvent event, String id) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();

        ProtectedPlayer.get(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            if (protectedPlayer.getDiscord() != event.getUser().getIdLong()) {
                event.reply(discordConfig.getMessage("account-not-linked")).setEphemeral(true).queue();
                return;
            }

            if (discordConfig.isDisableUnlink()) {
                event.reply(discordConfig.getMessage("unlink-disabled")).setEphemeral(true).queue();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setDiscord(0).setBanned(false);
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                event.reply(discordConfig.getMessage("account-unlink-success"))
                        .setEphemeral(true)
                        .queue();
                return protectedPlayer;
            });
        });
    }
}
