package com.beacmc.beacmcauth.core.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;

import java.util.UUID;

public class RecoveryCooldown extends AbstractCooldown<UUID> {

    private static RecoveryCooldown instance;

    public static RecoveryCooldown getInstance() {
        if (instance == null)
            instance = new RecoveryCooldown();
        return instance;
    }
}
