package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.concurrent.TimeUnit;

public class LoginRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String message;
    private final AuthManager authManager;
    private final Integer maxTimeAuth;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private final int messageSendDelay;
    private final boolean disableSendMessage;
    private Integer timer;
    private Integer waitForConnect;

    public LoginRunnable(BeacmcAuth plugin, ServerPlayer player) {
        this.player = player;
        this.plugin = plugin;
        Config config = plugin.getConfig();

        disableSendMessage = config.isDialogEnabled() && player.isNewerThanOrEqualsVersion(ClientVersion.V_1_21_6);

        timer = 0;
        waitForConnect = 0;
        maxTimeAuth = config.getTimePerLogin();
        authManager = plugin.getAuthManager();

        title = config.getMessages().getLoginTitle();
        subtitle = config.getMessages().getLoginSubtitle();
        message = config.getMessages().getLoginChat();
        messageSendDelay = config.getLoginMessageSendDelaySeconds();
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

        if (!authManager.getAuthPlayers().contains(player.getLowercaseName())) {
            task.cancel();
            return;
        }

        if (!player.isConnected()) {
            if (waitForConnect >= 10) {
                task.cancel();
            }
            waitForConnect++;
            return;
        }

        timer += 1;

        if (timer >= maxTimeAuth) {
            player.disconnect(config.getMessages().getTimeIsUp());
            authManager.getAuthPlayers().removeById(player.getName().toLowerCase());
            task.cancel();
            return;
        }

        if (timer % messageSendDelay == 0 && !disableSendMessage) {
            player.sendMessage(message);
            player.sendTitle(title, subtitle, in, stay, out);
        }
    }
}
