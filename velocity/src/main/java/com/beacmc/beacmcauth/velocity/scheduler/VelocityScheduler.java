package com.beacmc.beacmcauth.velocity.scheduler;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class VelocityScheduler implements TaskScheduler {

    private ScheduledTask task;

    private final BeacmcAuth plugin;
    private final ProxyServer proxyServer;

    public VelocityScheduler(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.proxyServer = plugin.getProxy().getOriginalProxyServer();
    }

    @Override
    public TaskScheduler runTaskDelay(Runnable runnable, long delay, TimeUnit timeUnit) {
        task = proxyServer.getScheduler()
                .buildTask(VelocityBeacmcAuth.getInstance(), runnable)
                .repeat(delay, timeUnit)
                .schedule();
        return this;
    }

    @Override
    public TaskScheduler runTask(Runnable runnable) {
        task = proxyServer.getScheduler()
                .buildTask(VelocityBeacmcAuth.getInstance(), runnable)
                .schedule();
        return this;
    }

    @Override
    public void cancel() {
        assert task != null;
        task.cancel();
    }
}
