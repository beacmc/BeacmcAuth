package com.beacmc.beacmcauth.core.packet;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.packet.PlayerPositionTracker;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BasePlayerPositionTracker implements PlayerPositionTracker, PacketListener {

    private final Map<UUID, Vector3d> locations;
    @Getter
    private boolean enabled;

    public BasePlayerPositionTracker(BeacmcAuth plugin) {
        locations = new HashMap<>();
        enabled = false;
        if (plugin.getProxy().getPlugin("packetevents") != null) {
            PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.HIGHEST);
            enabled = true;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
            User user = PacketEvents.getAPI().getPlayerManager().getUser(event.getPlayer());
            locations.put(user.getUUID(), packet.getPosition());
        }
    }

    @Override
    public @NotNull Vector3d getPlayerPosition(UUID uuid) {
        return locations.getOrDefault(uuid, new Vector3d(0, 64, 0));
    }
}
