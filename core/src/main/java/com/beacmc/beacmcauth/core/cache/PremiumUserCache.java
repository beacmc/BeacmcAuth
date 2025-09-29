package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.cache.Cache;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PremiumUserCache implements Cache<PremiumUser, String> {

    private final List<PremiumUser> caches = new ArrayList<>();

    @Override
    public PremiumUser getCacheData(String s) {
        PremiumUser user = Cache.super.getCacheData(s);
        return user != null && user.isExpired() ? null : user;
    }
}
