package com.beacmc.beacmcauth.api.packet;

import com.github.retrooper.packetevents.util.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerPositionTracker {

    @NotNull Vector3d getPlayerPosition(UUID uuid);

    boolean isEnabled();
}
