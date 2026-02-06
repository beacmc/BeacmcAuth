package com.beacmc.beacmcauth.api.server;

import com.beacmc.beacmcauth.api.server.player.ServerPlayer;

import java.util.Collection;

public interface Server {

    String getName();

    int getOnlinePlayersSize();

    Collection<ServerPlayer> getOnlinePlayers();

    <T> T getOriginalServer();

    boolean equals(Object obj);
}
