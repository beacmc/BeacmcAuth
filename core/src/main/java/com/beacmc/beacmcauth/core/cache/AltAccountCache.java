package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.model.AltAccounts;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ToString
public class AltAccountCache implements Cache<AltAccounts, String> {

    private final Map<String, AltAccounts> caches = new ConcurrentHashMap<>();
}
