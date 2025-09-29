package com.beacmc.beacmcauth.core.cache.cooldown;

import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;

public class GameCooldown extends AbstractCooldown<String> {

    private static GameCooldown instance;

    public static GameCooldown getInstance() {
        if (instance == null)
            instance = new GameCooldown();
        return instance;
    }
}
