package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.auth.AuthenticatingPlayer;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ToString
public class AuthenticatingPlayersCache implements Cache<AuthenticatingPlayer, String> {

    private final Map<String, AuthenticatingPlayer> caches = new ConcurrentHashMap<>();
}
