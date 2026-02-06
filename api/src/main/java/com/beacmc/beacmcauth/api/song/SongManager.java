package com.beacmc.beacmcauth.api.song;

import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import cz.koca2000.nbs4j.Song;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SongManager {

    default @Nullable Song findFirstSong() {
        return getSongs().stream()
                .findFirst()
                .orElse(null);
    }

    void loadSongs(Path dir);

    Song findRandomSong();

    void play(ServerPlayer player, Song song);

    void stop(UUID uuid);

    void stopAll();

    boolean isEnabled();

    List<Song> getSongs();

    Map<Integer, String> getInstrumentNotes();
}
