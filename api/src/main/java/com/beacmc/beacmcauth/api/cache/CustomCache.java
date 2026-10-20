package com.beacmc.beacmcauth.api.cache;

import java.util.Map;

public class CustomCache<T extends CachedData<ID>, ID> extends Cache<T, ID> {
    public CustomCache(Map<ID, T> caches) {
        super(caches);
    }
}
