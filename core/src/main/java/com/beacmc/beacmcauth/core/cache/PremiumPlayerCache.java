package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.auth.premium.PremiumPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class PremiumPlayerCache implements Cache<PremiumPlayer, String> {

    private final List<PremiumPlayer> caches = Collections.synchronizedList(new ArrayList<>());
}
