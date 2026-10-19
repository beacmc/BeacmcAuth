package com.beacmc.beacmcauth.api.cache;

import java.util.concurrent.ConcurrentHashMap;

public final class ConcurrentCache<T extends CachedData<ID>, ID> extends Cache<T, ID> {
    public ConcurrentCache() {
        super(new ConcurrentHashMap<>());
    }
}
