package com.beacmc.beacmcauth.core.util;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FutureUtil {

    public static <T> T await(CompletableFuture<T> future, T def) {
        try {
            return future.get();
        } catch (Exception ignored) {
            return def;
        }
    }

    @Nullable
    public static <T> T await(CompletableFuture<T> future) {
        return await(future, null);
    }
}
