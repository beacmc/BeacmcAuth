package com.beacmc.beacmcauth.velocity.server;

import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.velocity.player.VelocityServerPlayer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Collection;
import java.util.stream.Collectors;

public class VelocityServer implements Server {

    private final RegisteredServer server;

    public VelocityServer(RegisteredServer server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return server != null ? server.getServerInfo().getName() : null;
    }

    @Override
    public int getOnlinePlayersSize() {
        return server.getPlayersConnected().size();
    }

    @Override
    public Collection<ServerPlayer> getOnlinePlayers() {
        return server.getPlayersConnected().stream()
                .map(VelocityServerPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public <T> T getOriginalServer() {
        return (T) server;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj instanceof String) {
            return obj.equals(getName());
        }
        return obj.toString().equals(this.toString());
    }

    @Override
    public String toString() {
        return server.toString();
    }
}
