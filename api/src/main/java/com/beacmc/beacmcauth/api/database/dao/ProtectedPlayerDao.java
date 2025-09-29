package com.beacmc.beacmcauth.api.database.dao;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface ProtectedPlayerDao {

    ProtectedPlayer queryForId(UUID id) throws SQLException;

    int update(ProtectedPlayer player) throws SQLException;

    Dao.CreateOrUpdateStatus createOrUpdate(ProtectedPlayer player) throws SQLException;

    List<ProtectedPlayer> queryForEq(String fieldName, Object value) throws SQLException;

    int executeRaw(String var1, String... var2) throws SQLException;

    TableInfo<ProtectedPlayer, UUID> getTableInfo();

    int delete(ProtectedPlayer player) throws SQLException;

    int create(ProtectedPlayer player) throws SQLException;

    QueryBuilder<ProtectedPlayer, UUID> queryBuilder();
}
