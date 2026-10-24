package com.beacmc.beacmcauth.core.crypto;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Argon2Settings;
import com.beacmc.beacmcauth.api.crypto.CryptoExecutor;
import com.beacmc.beacmcauth.api.crypto.hash.Hash;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Argon2Hash implements Hash {

    @EqualsAndHashCode.Exclude
    private final BeacmcAuth plugin;
    private final String id;
    private final @NotNull Argon2Factory.Argon2Types type;

    @EqualsAndHashCode.Exclude
    private Argon2 argon2;

    private static final ExecutorService service = CryptoExecutor.getExecutor();

    @Override
    public void init() {
        argon2 = Argon2Factory.create(type);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CompletableFuture<String> hash(String password) {
        return CompletableFuture.supplyAsync(() -> {
            Argon2Settings settings = plugin.getConfig().getArgon2Settings();
            return argon2.hash(
                    settings.getIterations(),
                    settings.getMemoryKb(),
                    settings.getParallelism(),
                    password.getBytes(StandardCharsets.UTF_8)
            );
        }, service);
    }

    @Override
    public CompletableFuture<Boolean> matches(String hashedPassword, String rawPassword) {
        return CompletableFuture.supplyAsync(
                () -> argon2.verify(
                        hashedPassword,
                        rawPassword.getBytes(StandardCharsets.UTF_8)),
                service);
    }
}
