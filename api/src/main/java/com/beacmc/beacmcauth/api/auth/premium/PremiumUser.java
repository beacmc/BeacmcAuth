package com.beacmc.beacmcauth.api.auth.premium;

import com.beacmc.beacmcauth.api.cache.CachedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * Temporarily cached premium user
 */
@Getter
@ToString
@AllArgsConstructor
public class PremiumUser implements CachedData<String> {

    /**
     * Keeps the last name of the player
     */
    private final String id;
    /**
     * Store a premium unique ide user
     */
    private final UUID uuid;
    /**
     * Keeps the life of a cache
     */
    private final long limeTimeMillis;

    /**
     * @return Truth if the cache time has not yet happened
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= limeTimeMillis;
    }
}
