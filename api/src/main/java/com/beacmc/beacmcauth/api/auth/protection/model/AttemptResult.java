package com.beacmc.beacmcauth.api.auth.protection.model;

public record AttemptResult(
        boolean accountLocked,
        boolean ipLocked,
        boolean distributedAttackDetected,
        int accountFailures,
        int ipFailures
) {
}
