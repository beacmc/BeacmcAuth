package com.beacmc.beacmcauth.api.event;

import com.beacmc.beacmcauth.ProtectedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class AccountRegisterEvent extends Event implements Cancellable {

    private boolean cancel;
    private final ProtectedPlayer protectedPlayer;

    public AccountRegisterEvent(ProtectedPlayer protectedPlayer) {
        this.protectedPlayer = protectedPlayer;
        cancel = false;
    }

    public ProtectedPlayer getProtectedPlayer() {
        return protectedPlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
