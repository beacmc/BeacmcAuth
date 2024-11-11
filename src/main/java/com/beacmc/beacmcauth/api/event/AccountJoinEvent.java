package com.beacmc.beacmcauth.api.event;

import com.beacmc.beacmcauth.ProtectedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class AccountJoinEvent extends Event {

    private final ProtectedPlayer protectedPlayer;

    public AccountJoinEvent(ProtectedPlayer protectedPlayer) {
        this.protectedPlayer = protectedPlayer;
    }

    public ProtectedPlayer getProtectedPlayer() {
        return protectedPlayer;
    }
}
