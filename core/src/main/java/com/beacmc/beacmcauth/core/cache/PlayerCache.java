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
    public List<ProtectedPlayer> getCaches() {
        return caches;
    }
}
