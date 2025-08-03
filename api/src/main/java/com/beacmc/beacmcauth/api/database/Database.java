package com.beacmc.beacmcauth.api.database;

import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.j256.ormlite.support.ConnectionSource;

public interface Database {

    void init();

    ConnectionSource getConnectionSource();

    ProtectedPlayerDao getProtectedPlayerDao();
}
