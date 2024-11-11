package com.beacmc.beacmcauth.util.runnable.impl;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.config.impl.TelegramConfig;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.telegram.TelegramProvider;
import com.beacmc.beacmcauth.util.runnable.BaseRunnable;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

public class TelegramRunnable extends BaseRunnable {

    private final Title title;
    private final BaseComponent[] loginMessage;
    private final TelegramProvider telegram;
    private final Integer maxTimeConfirmation;
    private Integer timer;

    public TelegramRunnable(ProtectedPlayer protectedPlayer) {
        super(protectedPlayer);
        BaseConfig config = BeacmcAuth.getConfig();
        TelegramConfig telegramConfig = BeacmcAuth.getTelegramConfig();

        timer = 0;
        maxTimeConfirmation = telegramConfig.getTimePerConfirm();
        telegram = BeacmcAuth.getTelegramProvider();

        title = getProxyServer().createTitle();
        loginMessage = config.getMessage("telegram-confirmation-chat");

        title.title(config.getMessage("telegram-confirmation-title"))
                .subTitle(config.getMessage("telegram-confirmation-subtitle"))
                .stay(80)
                .fadeIn(0)
                .fadeOut(0);

        if (telegram != null)
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

        if (!telegram.getConfirmationUsers().containsKey(player.getDisplayName().toLowerCase()) || !player.isConnected()) {
            getTask().cancel();
            return;
        }

        timer += 1;

        if (timer >= maxTimeConfirmation) {
            player.disconnect(config.getMessage("time-is-up"));
            telegram.getConfirmationUsers().remove(player.getDisplayName().toLowerCase());
            getTask().cancel();
            return;
        }

        player.sendMessage(loginMessage);
        player.sendTitle(title);
    }
}
