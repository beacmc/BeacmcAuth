package com.beacmc.beacmcauth.api.cache;

import java.util.HashMap;

public final class SimpleCache<T extends CachedData<ID>, ID> extends Cache<T, ID> {
    public SimpleCache() {
        super(new HashMap<>());
    }
}
