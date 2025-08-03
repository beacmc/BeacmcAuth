package com.beacmc.beacmcauth.core.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;

public class TelegramCooldown extends AbstractCooldown<Long> {

    private static TelegramCooldown instance;

    public static TelegramCooldown getInstance() {
        if (instance == null)
            instance = new TelegramCooldown();
        return instance;
    }
}
