package com.beacmc.beacmcauth.api.auth.premium.mojang;

public interface PremiumChangerProvider<T> {

    void forceOfflineMode(T obj);

    void forceOnlineMode(T obj);
}
