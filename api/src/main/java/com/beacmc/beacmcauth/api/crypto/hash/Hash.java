package com.beacmc.beacmcauth.api.crypto.hash;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface Hash {

    String id();

    CompletableFuture<String> hash(String password);

    CompletableFuture<Boolean> matches(String hashedPassword, String rawPassword);

    default void init() {}

    default CompletableFuture<HashedPassword> migrate(String password) {
        return hash(password).thenCompose(hashed ->
                CompletableFuture.completedFuture(new HashedPassword(id(), hashed)));
    }

    default boolean isMigrateAvailable(@NotNull HashedPassword password) {
        return isMigrateAvailable(password.getHashId());
    }

    default boolean isMigrateAvailable(@NotNull Hash other) {
        return isMigrateAvailable(other.id());
    }

    default boolean isMigrateAvailable(String id) {
        return !id.equals(id());
    }
}
