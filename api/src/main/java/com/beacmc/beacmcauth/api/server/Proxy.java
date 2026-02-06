package com.beacmc.beacmcauth.api.server;

import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface Proxy {

    <T> void callEvent(T event);

    Server getServer(String name);

    ServerPlayer getPlayer(UUID uuid);

    ServerPlayer getPlayer(String name);

    List<ServerPlayer> getPlayers();

    ProxyType getProxyType();

    void registerListener(Object listener);

    Server[] getAllServers();

    void shutdown();

    TaskScheduler runTaskDelay(Runnable runnable, long delay, TimeUnit timeUnit);

    TaskScheduler runTask(Runnable runnable);

    void sendData(String channel, byte[] message, Server... servers);

    void registerChannel(String channel);

    void unregisterChannel(String channel);

    <T> T getOriginalProxyServer();

    <T> T getPlugin(@NotNull String name);
}
