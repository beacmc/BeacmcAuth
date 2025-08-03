package com.beacmc.beacmcauth.core.model;

public class CooldownUser<ID> {

    private ID id;
    private long cooldown;

    public CooldownUser(ID id, long cooldown) {
        this.id = id;
        this.cooldown = cooldown;
    }

    public CooldownUser(ID id) {
        this(id, 10000L);
    }

    public ID getID() {
        return id;
    }

    public long getCooldown() {
        return cooldown;
    }
}
