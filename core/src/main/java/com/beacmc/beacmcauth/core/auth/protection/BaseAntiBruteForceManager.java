package com.beacmc.beacmcauth.core.auth.protection;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.protection.AntiBruteForceManager;
import com.beacmc.beacmcauth.api.auth.protection.model.AttemptResult;
import com.beacmc.beacmcauth.api.auth.protection.model.RiskContext;
import com.beacmc.beacmcauth.api.auth.protection.model.RiskType;
import com.beacmc.beacmcauth.api.config.AntiBruteForceSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseAntiBruteForceManager implements AntiBruteForceManager {

    private final BeacmcAuth plugin;
    private final Map<String, RiskContext> riskContexts;
    private final DistributedAttackDetector distributedDetector;

    public BaseAntiBruteForceManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.riskContexts = new ConcurrentHashMap<>();
        this.distributedDetector = new DistributedAttackDetector(plugin);
    }

    @Override
    public AttemptResult recordFailedAttempt(String playerName, String ipAddress) {
        AntiBruteForceSettings settings = plugin.getProtectionConfig().getAntiBruteForceSettings();

        String account = normalizeAccount(playerName);
        String ip = normalizeIp(ipAddress);

        long now = System.currentTimeMillis();

        RiskContext accountContext = recordFailure(
                accountKey(account),
                RiskType.ACCOUNT_LIMITER_BRUTE_FORCE,
                settings.getAccountLimiter(),
                now
        );

        RiskContext ipContext = recordFailure(
                ipKey(ip),
                RiskType.IP_LIMITER_BRUTE_FORCE,
                settings.getIpLimiter(),
                now
        );

        boolean distributedAttackDetected =
                distributedDetector.recordFailedAttempt(ip, now);

        return new AttemptResult(
                accountContext.isLocked(now),
                ipContext.isLocked(now),
                distributedAttackDetected,
                accountContext.getFailedAttempts(),
                ipContext.getFailedAttempts()
        );
    }

    @Override
    public void clearAccountFailures(String playerName) {
        riskContexts.remove(
                accountKey(normalizeAccount(playerName))
        );
    }

    @Override
    public void cleanupExpired() {
        long now = System.currentTimeMillis();

        riskContexts.entrySet().removeIf(entry ->
                !entry.getValue().isValid(now));

        distributedDetector.cleanup(now);
    }

    @Override
    public boolean isIpLocked(String ip) {
        return isLocked(ipKey(ip));
    }

    @Override
    public boolean isAccountLocked(String playerName) {
        return isLocked(accountKey(playerName));
    }

    private boolean isLocked(String key) {
        long now = System.currentTimeMillis();
        RiskContext context = riskContexts.get(key);

        if (context == null) {
            return false;
        }

        if (!context.isValid(now)) {
            riskContexts.remove(key, context);
            return false;
        }

        return context.isLocked(now);
    }

    private RiskContext recordFailure(
            String key,
            RiskType riskType,
            AntiBruteForceSettings.Limiter limiter,
            long now
    ) {
        return riskContexts.compute(key, (ignored, current) -> {
            if (current != null && current.isLocked(now)) {
                return current;
            }

            if (current == null || !current.isValid(now)) {
                current = new RiskContext(
                        key,
                        riskType,
                        limiter.getTrackingExpiresAtMillis(),
                        limiter.getMaxFailures()
                );
            }

            int failures = current.incrementFailedAttempt();

            if (failures >= current.getMaxFailedAttempts()) {
                current.blockUntil(limiter.getLockExpiresAtMillis());
            }

            return current;
        });
    }

    private static String accountKey(String account) {
        return "account:" + normalizeAccount(account);
    }

    private static String ipKey(String ip) {
        return "ip:" + normalizeIp(ip);
    }

    private static String normalizeAccount(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }

        return playerName.trim().toLowerCase();
    }

    private static String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new IllegalArgumentException("IP address must not be blank");
        }

        return ipAddress.trim().toLowerCase();
    }
}
