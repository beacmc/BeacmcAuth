package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.social.TelegramConfig;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;

import java.util.concurrent.TimeUnit;

public class TelegramRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String message;
    private final Integer maxTimeAuth;
    private final SocialManager manager;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private final ProtectedPlayer protectedPlayer;
    private final int messageSendDelay;
    private Integer timer;

    public TelegramRunnable(BeacmcAuth plugin, ServerPlayer player, ProtectedPlayer protectedPlayer) {
        this.player = player;
        this.plugin = plugin;
        this.manager = plugin.getSocialManager();
        this.protectedPlayer = protectedPlayer;
        Config config = plugin.getConfig();
        TelegramConfig telegramConfig = plugin.getTelegramConfig();

        timer = 0;

        maxTimeAuth = telegramConfig.getTimePerConfirm();
        title = config.getMessages().getTelegramConfirmationTitle();
        subtitle = config.getMessages().getTelegramConfirmationSubtitle();
        message = config.getMessages().getTelegramConfirmationChat();

        in = 0;
        stay = 25;
        out = 0;
        messageSendDelay = telegramConfig.getMessageSendDelaySeconds();

        task = plugin.getProxy().runTaskDelay(this, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (player == null) {
            task.cancel();
            return;
        }

        final Config config = plugin.getConfig();
        final ConfirmationPlayer confirmationPlayer = manager.getConfirmationByPlayer(protectedPlayer);

        if (confirmationPlayer == null
                || confirmationPlayer.getCurrentConfirmation() == null
                || !manager.getConfirmationPlayers().contains(confirmationPlayer)
                || confirmationPlayer.getCurrentConfirmation().getType() != SocialType.TELEGRAM
                || !player.isConnected()) {
            task.cancel();
            plugin.getSongManager().stop(player.getUUID());
            return;
        }

        timer += 1;

        if (timer >= maxTimeAuth) {
            player.disconnect(config.getMessages().getTimeIsUp());
            manager.getConfirmationPlayers().remove(confirmationPlayer);
            task.cancel();
            plugin.getSongManager().stop(player.getUUID());
            return;
        }

        if (timer % messageSendDelay == 0) {
            player.sendMessage(message);
            player.sendTitle(title, subtitle, in, stay, out);
        }
    }
}
