package com.beacmc.beacmcauth.velocity.server.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.mojang.PremiumChangerProvider;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.song.SongManager;
import com.beacmc.beacmcauth.core.util.UUIDFetcher;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import com.beacmc.beacmcauth.velocity.player.VelocityServerPlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.*;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;

public class AuthListener {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;
    private final SongManager songManager;
    private final ServerLogger logger;

    public AuthListener(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
        this.logger = plugin.getServerLogger();
        this.songManager = plugin.getSongManager();
    }

    @Subscribe
    @SuppressWarnings("unchecked")
    public void onPreLogin(PreLoginEvent event) {
        Message disconnectMessage = authManager.onPremiumLogin(event.getUsername().toLowerCase(), (PremiumChangerProvider<? super PreLoginEvent>) VelocityBeacmcAuth.getInstance().getBeacmcAuth().getPremiumProvider(), event);
        if (disconnectMessage != null) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(disconnectMessage.toComponent()));
        }
    }

    @Subscribe
    public void onConnect(PlayerChooseInitialServerEvent event) {
        ServerPlayer player = new VelocityServerPlayer(event.getPlayer());
        authManager.onConnect(player).thenAccept(server -> {
            if (server != null) {
                event.setInitialServer(server.getOriginalServer());
            }
        }).exceptionally(e -> {
            logger.debug(e.getMessage());
            return null;
        });
    }

    @Subscribe
    public void onPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        if (authManager.isAuthenticating(player.getUsername())) {
            songManager.play(new VelocityServerPlayer(player), songManager.findRandomSong());
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        authManager.onDisconnect(new VelocityServerPlayer(event.getPlayer()));
    }

    @Subscribe
    public void onGameProfile(GameProfileRequestEvent event) {
        GameProfile profile = event.getGameProfile();
        event.setGameProfile(profile.withId(UUIDFetcher.byName(event.getUsername())));
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
