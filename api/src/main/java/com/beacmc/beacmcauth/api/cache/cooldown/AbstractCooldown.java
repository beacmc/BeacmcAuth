package com.beacmc.beacmcauth.api.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.Cache;

import java.util.ArrayList;
import java.util.List;

public class AbstractCooldown<ID> implements Cache<CooldownUser<ID>, ID> {

    private final List<CooldownUser<ID>> caches;

    public AbstractCooldown() {
        caches = new ArrayList<>();
    }

    @Override
    public CooldownUser<ID> updateCache(CooldownUser<ID> data) {
        CooldownUser<ID> oldData = getCacheData(data.getId());
        if (oldData == null)
            return null;

        caches.remove(oldData);
        caches.add(data);
        return data;
    }

    @Override
    public CooldownUser<ID> getCacheData(ID id) {
        return caches.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<CooldownUser<ID>> getCaches() {
        return caches;
    }

    public void createCooldown(ID id, long time) {
        CooldownUser<ID> user = new CooldownUser<>(id, System.currentTimeMillis() + time);
        addOrUpdateCache(user);
    }

    public boolean isCooldown(ID id) {
        CooldownUser<ID> user = getCacheData(id);
        return user != null && user.getCooldown() > System.currentTimeMillis();
    }
}
