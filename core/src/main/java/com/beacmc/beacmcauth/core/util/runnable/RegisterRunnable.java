package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public class RegisterRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String message;
    private final AuthManager authManager;
    private final Integer maxTimeAuth;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private final ServerLogger logger;
    private final int messageSendDelay;
    private Integer timer;

    public RegisterRunnable(BeacmcAuth plugin, ServerPlayer player) {
        this.player = player;
        this.plugin = plugin;
        Config config = plugin.getConfig();

        timer = 0;
        maxTimeAuth = config.getTimePerRegister();
        authManager = plugin.getAuthManager();

        title = config.getMessages().getRegisterTitle();
        subtitle = config.getMessages().getRegisterSubtitle();
        message = config.getMessages().getRegisterChat();
        messageSendDelay = config.getRegisterMessageSendDelaySeconds();
        in = 0;
        stay = messageSendDelay * 20 + 5;
        out = 0;

        logger = plugin.getServerLogger();
        task = plugin.getProxy().runTaskDelay(this, 1, TimeUnit.SECONDS);
        logger.debug("RegisterRunnable has started for player(" + player + ")");
    }

    @Override
    public void run() {
        if (player == null) {
            task.cancel();
            return;
        }

        final Config config = plugin.getConfig();

        if (!authManager.getAuthPlayers().containsKey(player.getName().toLowerCase()) || !player.isConnected()) {
            task.cancel();
            return;
        }

        timer += 1;

        if (timer >= maxTimeAuth) {
            player.disconnect(config.getMessages().getTimeIsUp());
            authManager.getAuthPlayers().remove(player.getName().toLowerCase());
            task.cancel();
            return;
        }

        if (timer % messageSendDelay == 0) {
            player.sendMessage(message);
            player.sendTitle(title, subtitle, in, stay, out);
        }
    }
}
