package com.beacmc.beacmcauth.core.database;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.core.cache.PlayerCache;
import com.beacmc.beacmcauth.core.database.dao.BaseProtectPlayerDao;
import com.beacmc.beacmcauth.core.library.Libraries;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BaseDatabase implements Database {

    private final ServerLogger logger;
    private final BeacmcAuth plugin;
    private ConnectionSource connectionSource;
    private ProtectedPlayerDao protectedPlayerDao;
    private Cache<ProtectedPlayer, UUID> playersCache;

    public BaseDatabase(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
        this.playersCache = new PlayerCache();
    }

    @Override
    public void init() {
        final DatabaseSettings databaseSettings = plugin.getConfig().getDatabaseSettings();

        try {
            loadDatabaseLibrary(databaseSettings.getType().name().toLowerCase());
            connectionSource = new JdbcConnectionSource(databaseSettings.getUrl(plugin), databaseSettings.getUsername(), databaseSettings.getPassword());
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
            TableUtils.createTableIfNotExists(connectionSource, ProtectedPlayer.class);
            migrate();
        } catch (Throwable e) {
            if (databaseSettings.isStopServerOnFailedConnection()) {
                logger.error("Database is not connected. Server stopping...");
                logger.error("error message: " + e.getMessage());
                plugin.getProxy().shutdown();
            }
        }

        if (connectionSource == null && databaseSettings.isStopServerOnFailedConnection()) {
            logger.error("Database is not connected. Server stopping...");
            plugin.getProxy().shutdown();
        }
    }

    private void migrate() throws SQLException {
        if (!getExistingColumns("auth_players").contains("vkontakte")) {
            protectedPlayerDao.executeRaw("ALTER TABLE `auth_players` ADD COLUMN vkontakte INTEGER DEFAULT 0;");
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
        }
        if (!getExistingColumns("auth_players").contains("vkontakte_2fa")) {
            protectedPlayerDao.executeRaw("ALTER TABLE `auth_players` ADD COLUMN vkontakte_2fa BOOLEAN DEFAULT true;");
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
        }
        if (!getExistingColumns("auth_players").contains("online_uuid")) {
            protectedPlayerDao.executeRaw("ALTER TABLE `auth_players` ADD COLUMN online_uuid CHAR(36) default NULL;");
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
        }
    }

    private List<String> getExistingColumns(String tableName) throws SQLException {
        List<String> columns = new LinkedList<>();

        DatabaseConnection connection = connectionSource.getReadOnlyConnection(tableName);
        try (CompiledStatement stmt = connection.compileStatement("PRAGMA table_info(" + tableName + ")", StatementBuilder.StatementType.SELECT, protectedPlayerDao.getTableInfo().getFieldTypes(), DatabaseConnection.DEFAULT_RESULT_FLAGS, true);
             DatabaseResults results = stmt.runQuery(null)) {

            while (results.next()) {
                String columnName = results.getString(1);
                columns.add(columnName.toLowerCase());
            }

        } catch (Exception ignore) {

        } finally {
            connectionSource.releaseConnection(connection);
        }

        return columns;
    }

    private void loadDatabaseLibrary(String databaseType) {
        final LibraryProvider libraryLoader = plugin.getLibraryProvider();

        switch (databaseType) {
            case "sqlite" -> libraryLoader.loadLibrary(Libraries.SQLITE);
            case "mariadb" -> libraryLoader.loadLibrary(Libraries.MARIADB);
            case "postgresql" -> libraryLoader.loadLibrary(Libraries.POSTGRESQL);
        }
    }

    @Override
    public ProtectedPlayerDao getProtectedPlayerDao() {
        return protectedPlayerDao;
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    @Override
    public Cache<ProtectedPlayer, UUID> getPlayersCache() {
        return playersCache;
    }
}
