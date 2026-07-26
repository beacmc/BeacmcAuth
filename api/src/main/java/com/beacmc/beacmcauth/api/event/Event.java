package com.beacmc.beacmcauth.api.event;

import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class Event {

    private final EventType type;
    private final ProtectedPlayer protectedPlayer;
    private final ServerPlayer serverPlayer;

    public abstract void handle(EventListener listener);
}
