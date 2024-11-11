package com.beacmc.beacmcauth.util.runnable;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public abstract class BaseRunnable implements Runnable {

    private ScheduledTask task;
    private final BeacmcAuth plugin;
    private final TaskScheduler scheduler;
    private final ProtectedPlayer protectedPlayer;
    private final ProxyServer proxyServer;

    public BaseRunnable(ProtectedPlayer protectedPlayer) {
        this.protectedPlayer = protectedPlayer;
        this.plugin = BeacmcAuth.getInstance();
        this.proxyServer = plugin.getProxy();
        this.scheduler = proxyServer.getScheduler();
    }

    public ScheduledTask runTask() {
        task = scheduler.runAsync(plugin, this);
        return task;
    }

    public ScheduledTask runTaskTimer(long delay, long period, TimeUnit timeUnit) {
        task = scheduler.schedule(plugin, this, delay, period, timeUnit);
        return task;
    }

    public ScheduledTask runTaskLater(long delay, TimeUnit timeUnit) {
        task = scheduler.schedule(plugin, this, delay, timeUnit);
        return task;
    }

    public ScheduledTask getTask() {
        return task;
    }

    public BeacmcAuth getPlugin() {
        return plugin;
    }

    public ProtectedPlayer getProtectedPlayer() {
        return protectedPlayer;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }
}
