package com.beacmc.beacmcauth.api.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.Cache;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
public class AbstractCooldown<ID> implements Cache<CooldownUser<ID>, ID> {

    private final Map<ID, CooldownUser<ID>> caches = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void createCooldown(ID id, long time) {
        CooldownUser<ID> user = new CooldownUser<>(id, System.currentTimeMillis() + time);
        addOrUpdateCache(user);
    }

    public boolean isCooldown(ID id) {
        CooldownUser<ID> user = getCacheData(id);
        return user != null && user.getCooldown() > System.currentTimeMillis();
    }
}
