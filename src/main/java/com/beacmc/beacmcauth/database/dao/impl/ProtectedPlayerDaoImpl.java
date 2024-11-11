package com.beacmc.beacmcauth.database.dao.impl;

import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class ProtectedPlayerDaoImpl extends BaseDaoImpl<ProtectedPlayer, String> implements ProtectedPlayerDao {

    public ProtectedPlayerDaoImpl(ConnectionSource source) throws SQLException {
        super(source, ProtectedPlayer.class);
    }
}
