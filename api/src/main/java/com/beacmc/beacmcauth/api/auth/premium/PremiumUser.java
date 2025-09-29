package com.beacmc.beacmcauth.api.auth.premium;

import com.beacmc.beacmcauth.api.cache.CachedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
public class PremiumUser implements CachedData<String> {

    private final String id;
    private final UUID uuid;
    private final long limeTimeMillis;

    public boolean isExpired() {
        return System.currentTimeMillis() >= limeTimeMillis;
    }
}
