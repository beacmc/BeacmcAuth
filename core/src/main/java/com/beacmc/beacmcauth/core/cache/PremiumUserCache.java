package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.cache.Cache;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PremiumUserCache implements Cache<PremiumUser, String> {

    private final Map<String, PremiumUser> caches = new ConcurrentHashMap<>();

    @Override
    public PremiumUser getCacheData(String id) {
        PremiumUser user = Cache.super.getCacheData(id);
        return user != null && user.isValid() ? user : null;
    }
}
