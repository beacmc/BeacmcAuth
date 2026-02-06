package com.beacmc.beacmcauth.api.config;

public interface AccountLimiterSettings {

    boolean isEnabled();

    int getLimit();
}
