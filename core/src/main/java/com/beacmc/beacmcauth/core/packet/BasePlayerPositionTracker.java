package com.beacmc.beacmcauth.core.packet;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.packet.position.PlayerPosition;
import com.beacmc.beacmcauth.api.packet.position.PlayerPositionTracker;
import com.beacmc.beacmcauth.core.packet.listener.PositionListener;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BasePlayerPositionTracker implements PlayerPositionTracker {

    private final Map<UUID, PlayerPosition> locations;
    @Getter
    private boolean enabled;

    public BasePlayerPositionTracker() {
        locations = new HashMap<>();
        enabled = false;
        PacketEvents.getAPI()
                .getEventManager()
                .registerListener(new PositionListener(locations), PacketListenerPriority.HIGHEST);
        enabled = true;
    }

    @Override
    public @NotNull PlayerPosition getPlayerPosition(UUID uuid) {
        return locations.getOrDefault(uuid, new PlayerPosition());
    }
}
