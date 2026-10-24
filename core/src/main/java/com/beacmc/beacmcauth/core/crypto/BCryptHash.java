package com.beacmc.beacmcauth.core.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.crypto.CryptoExecutor;
import com.beacmc.beacmcauth.api.crypto.hash.Hash;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@EqualsAndHashCode
public class BCryptHash implements Hash {

    @EqualsAndHashCode.Exclude
    private final BeacmcAuth plugin;
    private final String id;
    private final BCrypt.Version version;

    private static final ExecutorService service = CryptoExecutor.getExecutor();

    @Override
    public String id() {
        return id;
    }

    @Override
    public CompletableFuture<String> hash(String password) {
        return CompletableFuture.supplyAsync(() -> {
            Config config = plugin.getConfig();
            return BCrypt.with(version)
                    .hashToString(
                            config.getBCryptRounds(),
                            password.toCharArray()
                    );
        }, service);
    }

    @Override
    public CompletableFuture<Boolean> matches(String hashedPassword, String rawPassword) {
        return CompletableFuture.supplyAsync(
                () -> BCrypt.verifyer()
                        .verify(
                                rawPassword.toCharArray(),
                                hashedPassword.toCharArray()
                        ).verified,
                service);
    }
}
