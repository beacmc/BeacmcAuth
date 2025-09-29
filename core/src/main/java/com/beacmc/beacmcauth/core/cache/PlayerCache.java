package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class PlayerCache implements Cache<ProtectedPlayer, UUID> {

    private final List<ProtectedPlayer> caches = new ArrayList<>();
}
