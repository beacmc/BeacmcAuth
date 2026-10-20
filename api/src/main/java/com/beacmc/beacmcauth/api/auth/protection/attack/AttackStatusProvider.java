package com.beacmc.beacmcauth.api.auth.protection.attack;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface AttackStatusProvider {

    @NotNull Duration getCurrentAttackDuration();

    @NotNull Duration getLastAttackDuration();

    boolean isAttacking();
}
