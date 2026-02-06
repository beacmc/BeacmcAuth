package com.beacmc.beacmcauth.api.auth.premium;

import com.beacmc.beacmcauth.api.cache.CachedData;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PremiumUser implements CachedData<String> {

    private String name;
    private UUID uuid;
    private long lifetime;

    public boolean isValid() {
        return lifetime >= System.currentTimeMillis();
    }

    public static @NotNull PremiumUser create(@NotNull String name, @NotNull UUID uuid, @NotNull Duration lifetime) {
        long time = System.currentTimeMillis() + lifetime.toMillis();
        return new PremiumUser(name.toLowerCase(), uuid, time);
    }

    public static @Nullable PremiumUser create(ProtectedPlayer player, Duration lifetime) {
        UUID uuid = player.getOnlineUuid();
        return uuid != null ? create(player.getLowercaseName(), uuid, lifetime) : null;
    }

    @Override
    public String getId() {
        return name;
    }
}
