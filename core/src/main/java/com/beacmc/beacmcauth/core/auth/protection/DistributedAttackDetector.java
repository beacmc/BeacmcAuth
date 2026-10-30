package com.beacmc.beacmcauth.core.auth.protection;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.AntiBruteForceSettings;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
final class DistributedAttackDetector {

    private final BeacmcAuth plugin;
    private final Map<String, Long> uniqueIps = new ConcurrentHashMap<>();

    private long trackingExpiresAtMillis;
    private boolean attackDetected;

    synchronized boolean recordFailedAttempt(
            String ip,
            long now
    ) {
        AntiBruteForceSettings.Distributed settings = plugin.getProtectionConfig()
                .getAntiBruteForceSettings()
                .getDistributed();

        if (!settings.isEnabled()) {
            return false;
        }

        if (trackingExpiresAtMillis == 0 || now >= trackingExpiresAtMillis) {
            uniqueIps.clear();
            trackingExpiresAtMillis = settings.getTrackingExpiresAtMillis();
            attackDetected = false;
        }

        uniqueIps.putIfAbsent(ip, now);

        if (uniqueIps.size() >= settings.getUniqueIpsThreshold()) {
            attackDetected = true;
        }

        return attackDetected;
    }

    synchronized void cleanup(long now) {
        if (trackingExpiresAtMillis == 0 || now >= trackingExpiresAtMillis) {
            uniqueIps.clear();
            trackingExpiresAtMillis = 0;
            attackDetected = false;
        }
    }
}
