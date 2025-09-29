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
