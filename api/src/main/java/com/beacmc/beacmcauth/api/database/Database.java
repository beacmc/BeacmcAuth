package com.beacmc.beacmcauth.api.database;

import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.j256.ormlite.support.ConnectionSource;

import java.util.UUID;

public interface Database {

    void init();

    ConnectionSource getConnectionSource();

    ProtectedPlayerDao getProtectedPlayerDao();

    Cache<ProtectedPlayer, UUID> getPlayersCache();
}
