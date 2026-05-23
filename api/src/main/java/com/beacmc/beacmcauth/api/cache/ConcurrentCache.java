package com.beacmc.beacmcauth.api.cache;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class ConcurrentCache<T extends CachedData<ID>, ID> extends Cache<T, ID> {

    private final ConcurrentHashMap<ID, T> caches = new ConcurrentHashMap<>();
}
