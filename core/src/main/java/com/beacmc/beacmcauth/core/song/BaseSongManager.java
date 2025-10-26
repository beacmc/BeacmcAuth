package com.beacmc.beacmcauth.core.song;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.packet.PlayerPositionTracker;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.song.SongManager;
import com.beacmc.beacmcauth.api.song.SongPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import cz.koca2000.nbs4j.SongCorruptedException;
import lombok.Getter;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class BaseSongManager implements SongManager {

    private final List<Song> songs;
    private final Map<Integer, String> instrumentNotes;
    private final List<SongPlayer> playing;
    private final Random random;
    private final BeacmcAuth plugin;
    private final ServerLogger logger;
    private final boolean packetEventsEnabled;
    private final ExecutorService executorService;
    private final PlayerPositionTracker playerPositionTracker;

    public BaseSongManager(BeacmcAuth plugin) {
        this.songs = new ArrayList<>();
        this.instrumentNotes = new TreeMap<>();
        this.random = new Random();
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
        this.packetEventsEnabled = plugin.getProxy().getPlugin("packetevents") != null;
        this.playing = new ArrayList<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.playerPositionTracker = plugin.getPlayerPositionTracker();
    }

    @Override
    public void loadSongs(Path dir)  {
        File dirFile = dir.toFile();

        for (File file : Objects.requireNonNull(dirFile.listFiles())) {
            if (!file.isFile()) continue;

            if (file.getName().endsWith(".nbs")) {
                try {
                    Song song = Song.fromFile(file);
                    songs.add(song);
                } catch (IOException | SongCorruptedException e) {
                    e.printStackTrace();
                    logger.warn("Error loading music file %s: %s".formatted(file.getName(), e.getMessage()));
                }
                continue;
            }
            logger.debug("I found an incorrect %s file. Music files are only allowed with the '.nbs' extension.".formatted(file.getName()));
        }

        logger.log("Loaded %d songs!".formatted(songs.size()));
    }

    @Override
    public void play(ServerPlayer player, Song song) {
        if (!isEnabled() || player == null || song == null) return;

        System.out.println(isEnabled());

        SongPlayer songPlayer = new BaseSongPlayer(player, plugin);
        songPlayer.play(song);
        playing.add(songPlayer);
    }

    @Override
    public void stop(UUID uuid) {
        playing.stream()
                .filter(execute -> execute.getPlayer().getUUID().equals(uuid))
                .findFirst()
                .ifPresent(songPlayer -> {
                    songPlayer.stop();
                    playing.remove(songPlayer);
                });
    }

    @Override
    public void stopAll() {
        playing.forEach(songPlayer -> stop(songPlayer.getPlayer().getUUID()));
        playing.clear();
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().isNbsSongSupport() && packetEventsEnabled;
    }

    @Override
    public Song findRandomSong() {
        if (songs.isEmpty()) return null;

        int randomInt = random.nextInt(songs.size());
        return songs.get(randomInt);
    }
}
