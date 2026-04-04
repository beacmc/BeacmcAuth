package com.beacmc.beacmcauth.api.server.player;

import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.server.Server;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerClearDialog;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerShowDialog;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerClearDialog;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerShowDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerClearDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import org.jetbrains.annotations.Nullable;

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

    boolean isServerConnected();

    default void playSound(User user, Sound sound, float volume, float pitch, Vector3i location) {
        if (isServerConnected()) {
            WrapperPlayServerSoundEffect packet = new WrapperPlayServerSoundEffect(
                    sound,
                    SoundCategory.PLAYER,
                    location,
                    volume,
                    pitch
            );
            user.sendPacket(packet);
        }
    }

    default void playSound(Sound sound, float volume, float pitch, Vector3i location) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(getOriginalPlayer());
        playSound(user, sound, volume, pitch, location);
    }

    default void showDialog(@Nullable Dialog dialog) {
        if (dialog == null) {
            return;
        }

        if (isConnected() && isNewerThanOrEqualsVersion(ClientVersion.V_1_21_6)) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(getOriginalPlayer());
            WrapperCommonServerShowDialog<?> packet = user.getConnectionState() == ConnectionState.CONFIGURATION
                    ? new WrapperConfigServerShowDialog(dialog)
                    : new WrapperPlayServerShowDialog(dialog);
            user.sendPacket(packet);
        }
    }

    default void closeDialog() {
        if (isConnected() && isNewerThanOrEqualsVersion(ClientVersion.V_1_21_6)) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(getOriginalPlayer());
            WrapperCommonServerClearDialog<?> packet = user.getConnectionState() == ConnectionState.CONFIGURATION
                    ? new WrapperConfigServerClearDialog()
                    : new WrapperPlayServerClearDialog();
            user.sendPacket(packet);
        }
    }

    default boolean isNewerThanOrEqualsVersion(ClientVersion version) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(getOriginalPlayer());
        return user.getClientVersion().isNewerThanOrEquals(version);
    }

    Server getCurrentServer();

    <T> T getOriginalPlayer();

    boolean equals(Object obj);
}
