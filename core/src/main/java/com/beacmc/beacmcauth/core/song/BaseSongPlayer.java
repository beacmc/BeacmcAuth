package com.beacmc.beacmcauth.core.song;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.packet.PlayerPositionTracker;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.song.SongPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import cz.koca2000.nbs4j.CustomInstrument;
import cz.koca2000.nbs4j.Layer;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BaseSongPlayer extends SongPlayer {

    private final BeacmcAuth plugin;
    private final PlayerPositionTracker positionTracker;
    private boolean stopped = false;

    public BaseSongPlayer(@NotNull ServerPlayer player, @NotNull BeacmcAuth plugin) {
        super(player);
        this.plugin = plugin;
        this.positionTracker = plugin.getPlayerPositionTracker();
    }

    @Override
    public void play(Song song) {
        final int[] tick = {0};
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable[] task = new Runnable[1];
        task[0] = () -> {
            if (tick[0] > song.getSongLength() || !getPlayer().isConnected() || stopped) {
                return;
            }

            playTick(getPlayer(), song, tick[0]);

            double tempo = song.getTempo(tick[0]);
            long delay = (long) (1000 / tempo);

            tick[0]++;

            setScheduledFuture(scheduler.schedule(task[0], delay, TimeUnit.MILLISECONDS));
        };

        scheduler.submit(task[0]);
    }

    @Override
    public void stop() {
        stopped = true;
        if (getScheduledFuture() != null) getScheduledFuture().cancel(true);
    }

    public void playTick(ServerPlayer player, Song song, int tick) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player.getOriginalPlayer());
        if (!player.isConnected()) {
            return;
        }

        for (Layer layer : song.getLayers()) {
            Note note = layer.getNote(tick);
            if (note == null)
                continue;

            float pitch = (float) Math.pow(2.0, (note.getKey() - 45) / 12.0);
            int instrument = note.getInstrument();
            Vector3d pos = positionTracker.getPlayerPosition(getPlayer().getUUID());
            Vector3i location = new Vector3i(
                    (int) pos.x,
                    (int) pos.y,
                    (int) pos.z
            );
            String soundName;

            if (instrument >= 16) {
                int index = instrument - 16;
                if (index >= song.getCustomInstruments().size()) continue;
                CustomInstrument customInstrument = song.getCustomInstruments().get(index);
                soundName = customInstrument.getFileName();
            } else {
                soundName = getInstrumentName(instrument);
            }

            player.playSound(user, Sounds.getByName(soundName), note.getVolume(), pitch, location);
        }
    }

    private String getInstrumentName(int instrument) {
        return switch (instrument) {
            case 1 -> "block.note_block.bass";
            case 2 -> "block.note_block.basedrum";
            case 3 -> "block.note_block.snare";
            case 4 -> "block.note_block.hat";
            case 5 -> "block.note_block.guitar";
            case 6 -> "block.note_block.flute";
            case 7 -> "block.note_block.bell";
            case 8 -> "block.note_block.chime";
            case 9 -> "block.note_block.xylophone";
            case 10 -> "block.note_block.iron_xylophone";
            case 11 -> "block.note_block.cow_bell";
            case 12 -> "block.note_block.didgeridoo";
            case 13 -> "block.note_block.bit";
            case 14 -> "block.note_block.banjo";
            case 15 -> "block.note_block.pling";
            default -> "block.note_block.harp";
        };
    }
}
