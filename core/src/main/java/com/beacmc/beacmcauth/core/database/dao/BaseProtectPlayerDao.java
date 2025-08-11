package com.beacmc.beacmcauth.core.database.dao;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

public class BaseProtectPlayerDao extends BaseDaoImpl<ProtectedPlayer, String> implements ProtectedPlayerDao {

    private final Cache<ProtectedPlayer, String> players;

    public BaseProtectPlayerDao(BeacmcAuth plugin, ConnectionSource source) throws SQLException {
        super(source, ProtectedPlayer.class);
        this.players = plugin.getAuthManager().getPlayerCache();
    }

    @Override
    public synchronized CreateOrUpdateStatus createOrUpdate(ProtectedPlayer data) throws SQLException {
        if (data == null) return null;

        players.addOrUpdateCache(data);
        return super.createOrUpdate(data);
    }

    @Override
    public ProtectedPlayer queryForId(String id) throws SQLException {
        ProtectedPlayer cachedData = players.getCacheData(id);
        return cachedData != null ? cachedData : super.queryForId(id);
    }

    @Override
    public List<ProtectedPlayer> queryForEq(String fieldName, Object value) throws SQLException {
        List<ProtectedPlayer> result = super.queryForEq(fieldName, value);
        result.forEach(players::addOrUpdateCache);
        return result;
    }

    @Override
    public int update(ProtectedPlayer data) throws SQLException {
        if (data == null) return 0;
        players.addOrUpdateCache(data);
        return super.update(data);
    }

    @Override
    public int create(ProtectedPlayer data) throws SQLException {
        if (data == null) return 0;
        players.addOrUpdateCache(data);
        return super.create(data);
    }

    @Override
    public int delete(ProtectedPlayer data) throws SQLException {
        if (data == null) return 0;
        players.removeCache(data);
        return super.delete(data);
    }
}
