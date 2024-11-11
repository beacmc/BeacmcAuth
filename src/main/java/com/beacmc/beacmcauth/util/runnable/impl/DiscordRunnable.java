package com.beacmc.beacmcauth.util.runnable.impl;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.util.runnable.BaseRunnable;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DiscordRunnable extends BaseRunnable {

    private final Title title;
    private final BaseComponent[] loginMessage;
    private final DiscordProvider discord;
    private final Integer maxTimeConfirmation;
    private Integer timer;

    public DiscordRunnable(ProtectedPlayer protectedPlayer) {
        super(protectedPlayer);
        BaseConfig config = BeacmcAuth.getConfig();
        DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();

        timer = 0;
        maxTimeConfirmation = discordConfig.getTimePerConfirm();
        discord = BeacmcAuth.getDiscordProvider();

        title = getProxyServer().createTitle();
        loginMessage = config.getMessage("discord-confirmation-chat");

        title.title(config.getMessage("discord-confirmation-title"))
                .subTitle(config.getMessage("discord-confirmation-subtitle"))
                .stay(80)
                .fadeIn(0)
                .fadeOut(0);

        if (discord != null)
            runTaskTimer(0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (getProtectedPlayer().getPlayer() == null) {
            getTask().cancel();
            return;
        }

        final ProxiedPlayer player = getProtectedPlayer().getPlayer();
        final BaseConfig config = BeacmcAuth.getConfig();

        if (!discord.getConfirmationUsers().containsKey(player.getDisplayName().toLowerCase()) || !player.isConnected()) {
            getTask().cancel();
            return;
        }

        timer += 1;

        if (timer >= maxTimeConfirmation) {
            player.disconnect(config.getMessage("time-is-up"));
            discord.getConfirmationUsers().remove(player.getDisplayName().toLowerCase());
            getTask().cancel();
            return;
        }

        player.sendMessage(loginMessage);
        player.sendTitle(title);
    }
}
