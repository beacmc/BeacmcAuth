package com.beacmc.beacmcauth.core.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;

public class VkontakteCooldown extends AbstractCooldown<Integer> {

    private static VkontakteCooldown instance;

    public static VkontakteCooldown getInstance() {
        if (instance == null)
            instance = new VkontakteCooldown();
        return instance;
    }
}
