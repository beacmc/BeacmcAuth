package com.beacmc.beacmcauth.api.song;

import com.beacmc.beacmcauth.api.player.ServerPlayer;
import cz.koca2000.nbs4j.Song;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SongManager {

    default Song findFirstSong() {
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
