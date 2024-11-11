package com.beacmc.beacmcauth.discord.listener;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.api.event.AccountDiscordConfirmEvent;
import com.beacmc.beacmcauth.api.event.AccountLoginEvent;
import com.beacmc.beacmcauth.api.event.AccountRegisterEvent;
import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.telegram.TelegramProvider;
import com.beacmc.beacmcauth.util.runnable.impl.TelegramRunnable;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;


public class ConfirmationListener extends ListenerAdapter {

    private final AuthManager authManager;
    private final DiscordProvider discordProvider;
    private final TelegramProvider telegram;

    public ConfirmationListener(DiscordProvider discordProvider) {
        this.discordProvider = discordProvider;
        authManager = BeacmcAuth.getAuthManager();
        telegram = BeacmcAuth.getTelegramProvider();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        final Button button = event.getButton();
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final BaseConfig config = BeacmcAuth.getConfig();

        if (button.getId() != null && button.getId().startsWith("confirm-accept-")) {
            String[] split = button.getId().split("confirm-accept-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!discordProvider.getConfirmationUsers().containsKey(name)) {
                event.reply(discordConfig.getMessage("no-confirmation"))
                        .setEphemeral(true)
                        .queue();
                return;
            }
            ProtectedPlayer protectedPlayer = discordProvider.getConfirmationUsers().get(name);

            AccountDiscordConfirmEvent confirmEvent = new AccountDiscordConfirmEvent(protectedPlayer);
            ProxyServer.getInstance().getPluginManager().callEvent(confirmEvent);
            if (confirmEvent.isCancelled()) return;

            discordProvider.getConfirmationUsers().remove(name);
            ProxiedPlayer proxiedPlayer = protectedPlayer.getPlayer();
            event.reply(discordConfig.getMessage("confirmation-success"))
                    .setEphemeral(true)
                    .queue();

            proxiedPlayer.sendMessage(config.getMessage("discord-confirmation-success"));
            if (protectedPlayer.getTelegram() != 0 && telegram != null && protectedPlayer.isTelegramTwoFaEnabled()) {
                telegram.sendConfirmationMessage(protectedPlayer);
                telegram.getConfirmationUsers().put(protectedPlayer.getLowercaseName(), protectedPlayer);
                new TelegramRunnable(protectedPlayer);
                return;
            }

            protectedPlayer.performLogin();
            authManager.connectPlayerToServer(proxiedPlayer, config.findServerInfo(config.getGameServers()));
        }

        if (button.getId() != null && button.getId().startsWith("confirm-decline-")) {
            String[] split = button.getId().split("confirm-decline-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!discordProvider.getConfirmationUsers().containsKey(name)) {
                event.reply(discordConfig.getMessage("no-confirmation"))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            ProtectedPlayer player = discordProvider.getConfirmationUsers().get(name);
            discordProvider.getConfirmationUsers().remove(name);
            ProxiedPlayer proxiedPlayer = player.getPlayer();
            event.reply(discordConfig.getMessage("confirmation-denied"))
                    .setEphemeral(true)
                    .queue();

            if (proxiedPlayer != null) {
                proxiedPlayer.disconnect(config.getMessage("discord-confirmation-denied"));
            }
        }
    }
}
