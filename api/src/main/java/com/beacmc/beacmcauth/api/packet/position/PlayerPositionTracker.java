package com.beacmc.beacmcauth.api.packet.position;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerPositionTracker {

    @NotNull PlayerPosition getPlayerPosition(UUID uuid);

    boolean isEnabled();
}
