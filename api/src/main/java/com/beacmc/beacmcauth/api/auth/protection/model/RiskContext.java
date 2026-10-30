package com.beacmc.beacmcauth.api.auth.protection.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@RequiredArgsConstructor
public class RiskContext {

    private final String id;
    private final RiskType riskType;
    private final long trackingExpiresAtMillis;
    private final int maxFailedAttempts;

    @Getter(AccessLevel.NONE)
    private final AtomicInteger failedAttempts = new AtomicInteger();

    private volatile long lockExpiresAtMillis;

    public int incrementFailedAttempt() {
        return failedAttempts.incrementAndGet();
    }

    public int getFailedAttempts() {
        return failedAttempts.get();
    }

    public boolean isReachedMaxFailedAttempts() {
        return getFailedAttempts() >= maxFailedAttempts;
    }

    public void blockUntil(long expiresAtMillis) {
        lockExpiresAtMillis = expiresAtMillis;
    }

    public boolean isLocked() {
        return isLocked(System.currentTimeMillis());
    }

    public boolean isLocked(long currentTimeMillis) {
        return currentTimeMillis < lockExpiresAtMillis;
    }

    public boolean isTrackingExpired() {
        return isTrackingExpired(System.currentTimeMillis());
    }

    public boolean isTrackingExpired(long currentTimeMillis) {
        return currentTimeMillis >= trackingExpiresAtMillis;
    }

    public boolean isValid() {
        return isValid(System.currentTimeMillis());
    }

    public boolean isValid(long currentTimeMillis) {
        return !isTrackingExpired(currentTimeMillis)
                || isLocked(currentTimeMillis);
    }
}
