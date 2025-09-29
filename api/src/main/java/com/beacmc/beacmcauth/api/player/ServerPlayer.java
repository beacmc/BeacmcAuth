package com.beacmc.beacmcauth.api.player;

import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.server.Server;

import java.net.InetAddress;
import java.util.UUID;

public interface ServerPlayer extends CommandSender {

    String getName();

    default String getLowercaseName() {
        return getName().toLowerCase();
    }

    UUID getUUID();

    void connect(Server server);

    void disconnect(String message);

    void sendTitle(String title, String subtitle, long in, long stay, long out);

    InetAddress getInetAddress();

    boolean isConnected();

    Server getCurrentServer();

    <T> T getOriginalPlayer();

    boolean equals(Object obj);
}
