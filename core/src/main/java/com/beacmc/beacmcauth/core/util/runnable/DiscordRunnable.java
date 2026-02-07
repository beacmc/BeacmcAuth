package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.social.DiscordConfig;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;

import java.util.concurrent.TimeUnit;

public class DiscordRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String message;
    private final Integer maxTimeAuth;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private final SocialManager manager;
    private final ProtectedPlayer protectedPlayer;
    private final int messageSendDelay;
    private Integer timer;

    public DiscordRunnable(BeacmcAuth plugin, ServerPlayer player, ProtectedPlayer protectedPlayer) {
        this.player = player;
        this.plugin = plugin;
        this.protectedPlayer = protectedPlayer;
        this.manager = plugin.getSocialManager();
        Config config = plugin.getConfig();
        DiscordConfig discordConfig = plugin.getDiscordConfig();

        timer = 0;

        maxTimeAuth = discordConfig.getTimePerConfirm();
        title = config.getMessages().getDiscordConfirmationTitle();
        subtitle = config.getMessages().getDiscordConfirmationSubtitle();
        message = config.getMessages().getDiscordConfirmationChat();
        messageSendDelay = discordConfig.getMessageSendDelaySeconds();

        in = 0;
        stay = messageSendDelay * 20 + 5;
        out = 0;

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
                || confirmationPlayer.getCurrentConfirmation().getType() != SocialType.DISCORD
                || !player.isConnected()) {
            task.cancel();
            plugin.getSongManager().stop(player.getUUID());
            player.sendTitle("", "", 0, 10, 0);
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
