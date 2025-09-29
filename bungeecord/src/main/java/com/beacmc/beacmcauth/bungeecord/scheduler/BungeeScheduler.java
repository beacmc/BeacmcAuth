package com.beacmc.beacmcauth.bungeecord.scheduler;

import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class BungeeScheduler implements TaskScheduler {

    private final net.md_5.bungee.api.scheduler.TaskScheduler scheduler;
    private ScheduledTask task;

    public BungeeScheduler(net.md_5.bungee.api.scheduler.TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public TaskScheduler runTaskDelay(Runnable runnable, long delay, TimeUnit timeUnit) {
       task = scheduler.schedule(BungeeBeacmcAuth.getInstance(), runnable, 0, delay, timeUnit);
       return this;
    }

    @Override
    public TaskScheduler runTask(Runnable runnable) {
        task = scheduler.schedule(BungeeBeacmcAuth.getInstance(), runnable, 0, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public void cancel() {
        if (task != null) task.cancel();
    }
}
