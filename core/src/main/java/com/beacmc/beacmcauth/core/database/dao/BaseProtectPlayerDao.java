package com.beacmc.beacmcauth.core.database.dao;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BaseProtectPlayerDao extends BaseDaoImpl<ProtectedPlayer, UUID> implements ProtectedPlayerDao {

    private final Cache<ProtectedPlayer, UUID> players;

    public BaseProtectPlayerDao(BeacmcAuth plugin, ConnectionSource source) throws SQLException {
        super(source, ProtectedPlayer.class);
        this.players = plugin.getDatabase().getPlayersCache();
    }

    @Override
    public CreateOrUpdateStatus createOrUpdate(ProtectedPlayer data) throws SQLException {
        if (data == null) return null;

        players.addOrUpdateCache(data);
        return super.createOrUpdate(data);
    }

    @Override
    public ProtectedPlayer queryForId(UUID id) throws SQLException {
        ProtectedPlayer data = players.getCacheData(id);
        if (data != null) return data;

        data = super.queryForId(id);
        if (data != null) {
            players.addOrUpdateCache(data);
        }
        return data;
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
