package com.beacmc.beacmcauth.bungeecord.server.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.mojang.PremiumChangerProvider;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.song.SongManager;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;
import com.beacmc.beacmcauth.bungeecord.player.BungeeServerPlayer;
import com.beacmc.beacmcauth.bungeecord.server.BungeeServer;
import com.beacmc.beacmcauth.core.util.UUIDFetcher;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.UUID;

public class AuthListener implements Listener {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;
    private final SongManager songManager;
    private final ServerLogger logger;

    public AuthListener(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
        this.authManager = plugin.getAuthManager();
        this.songManager = plugin.getSongManager();
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onPreLogin(PreLoginEvent event) {
        final PendingConnection connection = event.getConnection();

        String name = connection.getName();
        UUID uuid = UUIDFetcher.byName(name);

        connection.setUniqueId(uuid);

        Message disconnectMessage = authManager.onPremiumLogin(name.toLowerCase(), (PremiumChangerProvider<? super PendingConnection>) BungeeBeacmcAuth.getInstance().getBeacmcAuth().getPremiumProvider(), connection);
        if (disconnectMessage != null) {
            event.setReason(disconnectMessage.toBaseComponent());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY) {
            ServerInfo defaultServer = event.getTarget();

            event.setCancelled(true);

            authManager.onConnect(new BungeeServerPlayer(player)).thenAccept(server -> {
                if (server != null) {
                    player.connect(server.getOriginalServer(), (connResult, throwable) -> {
                        if (throwable != null) {
                            player.connect(defaultServer);
                        }
                    });
                } else {
                    player.connect(defaultServer);
                }
            }).exceptionally(e -> {
                player.connect(defaultServer);
                return null;
            });
        }
    }

    @EventHandler
    public void onConnected(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (authManager.isAuthenticating(player.getName())) {
            songManager.play(new BungeeServerPlayer(player), songManager.findRandomSong());
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        authManager.onDisconnect(new BungeeServerPlayer(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        final Connection sender = event.getSender();
        final Config config = plugin.getConfig();
        final List<String> whitelistCommands = config.getWhitelistCommands();

        if (!(sender instanceof ProxiedPlayer proxiedPlayer)) {
            return;
        }

        ServerPlayer player = new BungeeServerPlayer(proxiedPlayer);
        if (authManager.isAuthenticating(player)) {
            String cmd = event.getMessage().split("\\s+")[0];
            if(event.isCommand() && whitelistCommands.contains(cmd.toLowerCase())) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event) {
        final Server server = new BungeeServer(event.getTarget());
        final ServerPlayer player = new BungeeServerPlayer(event.getPlayer());
        final Config config = plugin.getConfig();

        if (!authManager.isAuthenticating(player))
            return;

        if (config.getDisabledServers().stream().anyMatch(execute -> server.getName().equals(execute))) {
            player.sendMessage(config.getMessages().getBlockedServer());
            event.setCancelled(true);
        }
    }
}
