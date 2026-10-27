package com.beacmc.beacmcauth.api.config;

public interface AntiBruteForceSettings {

    Limiter getIpLimiter();

    Limiter getAccountLimiter();

    Distributed getDistributed();

    interface Limiter {

        int getMaxFailures();

        int getTrackingPeriodSeconds();

        int getLockDurationSeconds();
    }

    interface Distributed {

        boolean isEnabled();

        int getUniqueIpsThreshold();

        int getTrackingPeriodSeconds();
    }
}
