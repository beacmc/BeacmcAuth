package com.beacmc.beacmcauth.api.player;

import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.server.Server;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;

import java.net.InetAddress;
import java.util.UUID;

public interface ServerPlayer extends CommandSender {

    String getName();

    default String getLowercaseName() {
        return getName().toLowerCase();
    }

    UUID getUUID();

    void connect(Server server);

    void disconnect(String message);

    void sendTitle(String title, String subtitle, long in, long stay, long out);

    InetAddress getInetAddress();

    boolean isConnected();

    default void playSound(User user, Sound sound, float volume, float pitch, Vector3i location) {
        WrapperPlayServerSoundEffect packet = new WrapperPlayServerSoundEffect(
                sound,
                SoundCategory.PLAYER,
                location,
                volume,
                pitch
        );
        user.sendPacket(packet);
    }

    default void playSound(Sound sound, float volume, float pitch, Vector3i location) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(getOriginalPlayer());
        playSound(user, sound, volume, pitch, location);
    }

    Server getCurrentServer();

    <T> T getOriginalPlayer();

    boolean equals(Object obj);
}
