package com.beacmc.beacmcauth.listener;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.api.event.AccountJoinEvent;
import com.beacmc.beacmcauth.api.event.AccountLoginEvent;
import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AuthListener implements Listener {

    private final AuthManager auth;

    public AuthListener() {
        auth = BeacmcAuth.getAuthManager();
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        final String name = player.getDisplayName().toLowerCase();
        final BaseConfig config = BeacmcAuth.getConfig();

        if (!config.getNicknameRegex().matcher(name).matches()) {
            player.disconnect(config.getMessage("invalid-character-in-name", Map.of()));
            return;
        }

        ProtectedPlayer.get(name).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                try {
                    String ip = player.getAddress().getHostName();
                    long currentTime = System.currentTimeMillis();
                    CompletableFuture<ProtectedPlayer> completable = ProtectedPlayer.create(name, player.getDisplayName(), null, currentTime, currentTime, false, true, true, ip, ip, player.getUniqueId());
                    protectedPlayer = completable.get();
                    AccountJoinEvent joinEvent = new AccountJoinEvent(protectedPlayer);
                    ProxyServer.getInstance().getPluginManager().callEvent(joinEvent);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            auth.tryLogin(protectedPlayer);
        });
    }

    @EventHandler
    public void onDisonnect(PlayerDisconnectEvent event) {
        auth.disconnect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event) {
        final ServerInfo server = event.getTarget();
        final ProxiedPlayer player = event.getPlayer();
        final BaseConfig config = BeacmcAuth.getConfig();

        if (!auth.getAuthPlayers().containsKey(player.getDisplayName().toLowerCase()))
            return;

        if (config.getDisabledServers().stream().anyMatch(execute -> server.getName().equals(execute))) {
            player.sendMessage(config.getMessage("blocked-server", Map.of()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        final Connection sender = event.getSender();
        final BaseConfig config = BeacmcAuth.getConfig();
        final List<String> whitelistCommands = config.getWhitelistCommands();

        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (auth.getAuthPlayers().containsKey(player.getDisplayName().toLowerCase())) {
            String cmd = event.getMessage().split(" ")[0];
            if(event.isCommand() && whitelistCommands.contains(cmd.toLowerCase())) {
                return;
            }
            event.setCancelled(true);
        }
    }
}
