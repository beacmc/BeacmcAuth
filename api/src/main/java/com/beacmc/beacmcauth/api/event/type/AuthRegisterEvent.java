package com.beacmc.beacmcauth.api.event.type;

import com.beacmc.beacmcauth.api.event.Event;
import com.beacmc.beacmcauth.api.event.EventListener;
import com.beacmc.beacmcauth.api.event.EventType;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;

public class AuthRegisterEvent extends Event {
    public AuthRegisterEvent(ProtectedPlayer protectedPlayer, ServerPlayer serverPlayer) {
        super(EventType.REGISTER, protectedPlayer, serverPlayer);
    }

    @Override
    public void handle(EventListener listener) {
        listener.onRegister(this);
    }
}
