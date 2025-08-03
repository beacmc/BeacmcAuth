package com.beacmc.beacmcauth.core.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;

public class DiscordCooldown extends AbstractCooldown<Long> {

    private static DiscordCooldown instance;

    public static DiscordCooldown getInstance() {
        if (instance == null)
            instance = new DiscordCooldown();
        return instance;
    }
}
