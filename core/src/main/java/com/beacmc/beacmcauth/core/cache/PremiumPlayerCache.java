package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class PremiumPlayerCache implements Cache<PremiumPlayer, String> {

    private final Map<String, PremiumPlayer> caches = new HashMap<>();
}
