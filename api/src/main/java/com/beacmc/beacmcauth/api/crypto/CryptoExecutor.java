package com.beacmc.beacmcauth.api.crypto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptoExecutor {

    private static final int THREADS = 2;
    private static final int QUEUE_CAPACITY = 8;

    private static final ExecutorService SERVICE =
            new ThreadPoolExecutor(
                    THREADS,
                    THREADS,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                    Thread.ofPlatform()
                            .name("BeacmcAuth-Crypto-", 0)
                            .factory(),
                    new ThreadPoolExecutor.AbortPolicy()
            );

    public static ExecutorService getExecutor() {
        return SERVICE;
    }

    public static void shutdown() {
        SERVICE.shutdown();
    }
}
