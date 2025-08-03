package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;

import java.util.ArrayList;
import java.util.List;

public class PlayerCache implements Cache<ProtectedPlayer, String> {

    protected transient List<ProtectedPlayer> caches;

    public PlayerCache() {
        caches = new ArrayList<>();
    }

    @Override
    public ProtectedPlayer updateCache(ProtectedPlayer data) {
        ProtectedPlayer oldPlayer = getCacheData(data.getLowercaseName());
        if (oldPlayer == null)
            return null;

        caches.remove(oldPlayer);
        caches.add(data);
        return data;
    }

    @Override
    public ProtectedPlayer getCacheData(String id) {
        return caches.stream()
                .filter(cacheData -> cacheData.getLowercaseName().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ProtectedPlayer> getCaches() {
        return caches;
    }
}
