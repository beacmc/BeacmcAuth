package com.beacmc.beacmcauth.util.runnable.impl;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.util.runnable.BaseRunnable;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

public class LoginRunnable extends BaseRunnable {

    private final Title title;
    private final BaseComponent[] loginMessage;
    private final AuthManager authManager;
    private final Integer maxTimeAuth;
    private Integer timer;

    public LoginRunnable(ProtectedPlayer protectedPlayer) {
        super(protectedPlayer);
        BaseConfig config = BeacmcAuth.getConfig();

        timer = 0;
        maxTimeAuth = config.getTimePerLogin();
        authManager = BeacmcAuth.getAuthManager();

        title = getProxyServer().createTitle();
        loginMessage = config.getMessage("login-chat");

        title.title(config.getMessage("login-title"))
                .subTitle(config.getMessage("login-subtitle"))
                .stay(80)
                .fadeIn(0)
                .fadeOut(0);

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

        if (!authManager.getAuthPlayers().containsKey(player.getDisplayName().toLowerCase()) || !player.isConnected()) {
            getTask().cancel();
            return;
        }

        timer += 1;

        if (timer >= maxTimeAuth) {
            player.disconnect(config.getMessage("time-is-up"));
            authManager.getAuthPlayers().remove(player.getDisplayName().toLowerCase());
            getTask().cancel();
            return;
        }

        player.sendMessage(loginMessage);
        player.sendTitle(title);
    }
}
