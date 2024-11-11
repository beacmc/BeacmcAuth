package com.beacmc.beacmcauth.auth;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.api.event.AccountSessionActiveEvent;
import com.beacmc.beacmcauth.util.runnable.impl.LoginRunnable;
import com.beacmc.beacmcauth.util.runnable.impl.RegisterRunnable;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    private final Map<String, Integer> authorizationPlayers;
    private final BeacmcAuth plugin;
    private final ProxyServer proxy;

    public AuthManager() {
        authorizationPlayers = new HashMap<>();
        plugin = BeacmcAuth.getInstance();
        proxy = plugin.getProxy();
    }

    public void tryLogin(ProtectedPlayer player) {
        final BaseConfig config = BeacmcAuth.getConfig();
        final ProxiedPlayer proxiedPlayer = player.getPlayer();
        final String ip = proxiedPlayer.getAddress().getHostName();

        if (!config.getNicknameRegex().matcher(player.getRealName()).matches()) {
            proxiedPlayer.disconnect(config.getMessage("invalid-character-in-name"));
            return;
        }

        if (player.isBanned()) {
            proxiedPlayer.disconnect(config.getMessage("account-banned"));
            return;
        }

        if (config.isNameCaseControl() && !player.getRealName().equals(proxiedPlayer.getName())) {
            proxiedPlayer.disconnect(config.getMessage("name-case-failed", Map.of("%current_name%", proxiedPlayer.getDisplayName(), "%need_name%", player.getRealName())));
            return;
        }

        if (!player.isRegister()) {
            authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
            new RegisterRunnable(player);
            this.connectPlayerToServer(proxiedPlayer, config.findServerInfo(config.getAuthServers()));
            return;
        }

        if (player.isSessionActive() && player.isValidIp(ip)) {
            AccountSessionActiveEvent event = new AccountSessionActiveEvent(player);
            proxy.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.connectPlayerToServer(proxiedPlayer, config.findServerInfo(config.getGameServers()));
                proxiedPlayer.sendMessage(config.getMessage("session-active"));
            }
            return;
        }
        authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
        new LoginRunnable(player);
        this.connectPlayerToServer(proxiedPlayer, config.findServerInfo(config.getAuthServers()));
    }

    public void disconnect(ProxiedPlayer player) {
        authorizationPlayers.remove(player.getDisplayName().toLowerCase());
    }

    public void connectPlayerToServer(ProxiedPlayer player, ServerInfo server) {
        final BaseConfig config = BeacmcAuth.getConfig();

        if (server == null) {
            player.disconnect(config.getMessage("find-server-error"));
            return;
        }

        player.connect(server);
    }

    public Map<String, Integer> getAuthPlayers() {
        return authorizationPlayers;
    }
}
