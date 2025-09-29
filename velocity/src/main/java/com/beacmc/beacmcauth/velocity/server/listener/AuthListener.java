package com.beacmc.beacmcauth.velocity.server.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumProvider;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import com.beacmc.beacmcauth.velocity.player.VelocityServerPlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;

public class AuthListener {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public AuthListener(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        authManager.onLogin(new VelocityServerPlayer(event.getPlayer()));
    }

    @Subscribe
    @SuppressWarnings("unchecked")
    public void onPreLogin(PreLoginEvent event) {
        authManager.onPremiumLogin(event.getUsername().toLowerCase(), (PremiumProvider<? super PreLoginEvent>) VelocityBeacmcAuth.getInstance().getBeacmcAuth().getPremiumProvider(), event);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        authManager.onDisconnect(new VelocityServerPlayer(event.getPlayer()));
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        final ServerPlayer player = new VelocityServerPlayer(event.getPlayer());

        if (authManager.isAuthenticating(player)) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if (event.getCommandSource() instanceof ConsoleCommandSource) return;

        final Config config = plugin.getConfig();
        final ServerPlayer player = new VelocityServerPlayer((Player) event.getCommandSource());
        final String cmd = "/" + event.getCommand().split(" ")[0];

        if (authManager.isAuthenticating(player) && !config.getWhitelistCommands().contains(cmd)) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        final ServerPlayer player = new VelocityServerPlayer(event.getPlayer());
        final String serverName = event.getOriginalServer().getServerInfo().getName();
        final Config config = plugin.getConfig();

        if (authManager.isAuthenticating(player) && config.getDisabledServers().contains(serverName)) {
            player.sendMessage(config.getMessages().getBlockedServer());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }
}
