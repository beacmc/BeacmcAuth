package com.beacmc.beacmcauth.api.song;

import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import cz.koca2000.nbs4j.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class SongPlayer {

    private final ServerPlayer player;
    private int currentTick;
    private ScheduledFuture<?> scheduledFuture;

    public int getCurrentAndAdvanceTick() {
        return currentTick += 1;
    }

    public abstract void play(Song song);

    public abstract void stop();
}
