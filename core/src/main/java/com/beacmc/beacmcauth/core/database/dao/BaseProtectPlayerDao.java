package com.beacmc.beacmcauth.core.database.dao;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class BaseProtectPlayerDao extends BaseDaoImpl<ProtectedPlayer, String> implements ProtectedPlayerDao {

    public BaseProtectPlayerDao(ConnectionSource source) throws SQLException {
        super(source, ProtectedPlayer.class);
    }
}
