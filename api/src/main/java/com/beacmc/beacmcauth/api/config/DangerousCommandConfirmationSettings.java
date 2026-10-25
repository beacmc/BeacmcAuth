package com.beacmc.beacmcauth.api.config;

import java.util.concurrent.TimeUnit;

public interface DangerousCommandConfirmationSettings {

    boolean isEnabled();

    TimeUnit getConfirmationLifetimeUnit();

    long getConfirmationLifetime();

    default long getConfirmationLifetimeMillis() {
        return getConfirmationLifetimeUnit().toMillis(getConfirmationLifetime());
    }
}
