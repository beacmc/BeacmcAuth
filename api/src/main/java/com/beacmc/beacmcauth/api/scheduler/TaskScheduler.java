package com.beacmc.beacmcauth.api.scheduler;

import java.util.concurrent.TimeUnit;

public interface TaskScheduler {

    TaskScheduler runTaskDelay(Runnable runnable, long delay, TimeUnit timeUnit);

    void cancel();
}
