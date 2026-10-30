package com.beacmc.beacmcauth.api.config;

public interface AntiBruteForceSettings {

    Limiter getIpLimiter();

    Limiter getAccountLimiter();

    Distributed getDistributed();

    interface Limiter {

        int getMaxFailures();

        int getTrackingPeriodSeconds();

        int getLockDurationSeconds();

        default long getTrackingExpiresAtMillis() {
            return getTrackingPeriodSeconds() * 1000L + System.currentTimeMillis();
        }

        default long getLockExpiresAtMillis() {
            return getTrackingPeriodSeconds() * 1000L + System.currentTimeMillis();
        }
    }

    interface Distributed {

        boolean isEnabled();

        int getUniqueIpsThreshold();

        int getTrackingPeriodSeconds();

        boolean isKickAfterSingleLoginAttemptDuringAttack();

        default long getTrackingExpiresAtMillis() {
            return getTrackingPeriodSeconds() * 1000L + System.currentTimeMillis();
        }
    }
}
