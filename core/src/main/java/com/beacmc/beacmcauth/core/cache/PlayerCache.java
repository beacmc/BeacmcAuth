package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ToString
public class PlayerCache implements Cache<ProtectedPlayer, UUID> {

    private final Map<UUID, ProtectedPlayer> caches = new ConcurrentHashMap<>();
}
