package com.beacmc.beacmcauth.core.packet;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.packet.position.PlayerPosition;
import com.beacmc.beacmcauth.api.packet.position.PlayerPositionTracker;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.*;

public class BasePlayerPositionTracker implements PlayerPositionTracker {

    private final Map<UUID, PlayerPosition> locations;
    @Getter
    private boolean enabled;

    public BasePlayerPositionTracker(BeacmcAuth plugin) {
        locations = new HashMap<>();
        enabled = false;
        if (plugin.getProxy().getPlugin("packetevents") != null) {
            try {
                Class<?> packetEventsClass = Class.forName("com.github.retrooper.packetevents.PacketEvents");
                Object api = packetEventsClass.getMethod("getAPI").invoke(null);

                Object eventManager = api.getClass().getMethod("getEventManager").invoke(api);

                Class<?> listenerClass = Class.forName("com.beacmc.beacmcauth.core.packet.PositionListener");
                Object listener = listenerClass.getConstructor(Map.class).newInstance(locations);

                Class<?> priorityClass = Class.forName("com.github.retrooper.packetevents.event.PacketListenerPriority");
                Object highestPriority = Enum.valueOf((Class<Enum>) priorityClass, "HIGHEST");

                eventManager.getClass()
                        .getMethod("registerListener", Object.class, priorityClass)
                        .invoke(eventManager, listener, highestPriority);

                enabled = true;
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public @NotNull PlayerPosition getPlayerPosition(UUID uuid) {
        return locations.getOrDefault(uuid, new PlayerPosition());
    }
}
