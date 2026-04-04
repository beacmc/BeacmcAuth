package com.beacmc.beacmcauth.core.packet.listener;

import com.beacmc.beacmcauth.api.packet.position.PlayerPosition;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class PositionListener implements PacketListener {

    private final Map<UUID, PlayerPosition> locations;

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
            User user = event.getUser();
            if (user != null) {
                Vector3d pos = packet.getPosition();
                this.locations.put(user.getUUID(), new PlayerPosition(pos.x, pos.y, pos.z));
            }
        }
    }
}
