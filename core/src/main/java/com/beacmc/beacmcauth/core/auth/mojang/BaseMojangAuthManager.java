package com.beacmc.beacmcauth.core.auth.mojang;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.auth.premium.mojang.MojangAuthManager;
import com.beacmc.beacmcauth.api.auth.premium.response.Response;
import com.beacmc.beacmcauth.api.auth.premium.response.creator.GetPremiumUserResponse;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.MojangAuthConfig;
import com.beacmc.beacmcauth.core.cache.PremiumUserCache;
import com.beacmc.beacmcauth.core.util.UUIDFetcher;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class BaseMojangAuthManager implements MojangAuthManager {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private final Cache<PremiumUser, String> cache = new PremiumUserCache();
    private final BeacmcAuth plugin;

    @Getter
    private volatile long rateLimitTimeExpired = 0;

    @Override
    public CompletableFuture<Response<PremiumUser>> getPremiumUser(String name) {
        return CompletableFuture.supplyAsync(() -> {
            final MojangAuthConfig authConfig = plugin.getMojangAuthConfig();

            try {
                PremiumUser cached = cache.getCacheData(name.toLowerCase());
                if (cached != null) {
                    return new GetPremiumUserResponse(authConfig, -1, cached);
                }

                if (isRateLimited()) {
                    return new GetPremiumUserResponse(authConfig, authConfig.getRateLimitedCode(), null, rateLimitTimeExpired);
                }

                URL url = new URL(authConfig.getAuthUrl().formatted(name.toLowerCase()));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                int code = connection.getResponseCode();
                JsonObject json = JsonParser.parseReader(
                        new InputStreamReader(code >= 400
                                        ? connection.getErrorStream()
                                        : connection.getInputStream())
                ).getAsJsonObject();

                if (code == authConfig.getRateLimitedCode()) {
                    String retryAfterHeader = connection.getHeaderField(authConfig.getRateLimitRetryAfterField());
                    long retryAfterSeconds = retryAfterHeader != null
                            ? Long.parseLong(retryAfterHeader)
                            : 100;
                    rateLimitTimeExpired = System.currentTimeMillis() + retryAfterSeconds * 1_000L;
                    return new GetPremiumUserResponse(authConfig, code, null, rateLimitTimeExpired);
                } else if (code != authConfig.getUserFoundCode()) {
                    return new GetPremiumUserResponse(authConfig, code, null);
                }

                String uuidRaw = json.get(authConfig.getUniqueIdField()).getAsString();
                UUID uuid = UUIDFetcher.fromRaw(uuidRaw);

                PremiumUser user = PremiumUser.create(name.toLowerCase(), uuid, Duration.ofHours(1));
                cache.addOrUpdateCache(user);

                return new GetPremiumUserResponse(
                        authConfig,
                        code,
                        user
                );
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, EXECUTOR_SERVICE);
    }
}
