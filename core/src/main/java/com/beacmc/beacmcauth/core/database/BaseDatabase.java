package com.beacmc.beacmcauth.core.database;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.database.DatabaseType;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.core.cache.PlayerCache;
import com.beacmc.beacmcauth.core.database.dao.BaseProtectPlayerDao;
import com.beacmc.beacmcauth.core.library.Libraries;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;
import java.util.UUID;

public class BaseDatabase implements Database {

    private final ServerLogger logger;
    private final BeacmcAuth plugin;
    private final Cache<ProtectedPlayer, UUID> playersCache;
    private ConnectionSource connectionSource;
    private ProtectedPlayerDao protectedPlayerDao;

    public BaseDatabase(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
        this.playersCache = new PlayerCache();
    }

    @Override
    public void init() {
        final DatabaseSettings databaseSettings = plugin.getConfig().getDatabaseSettings();
        final DatabaseType type = databaseSettings.getType();

        try {
            Logger.setGlobalLogLevel(Level.WARNING);

            loadDatabaseLibrary(type);
            String url = databaseSettings.getUrl(plugin);

            HikariConfig config = databaseSettings.getHikariSettings().createHikariInstance();
            config.setJdbcUrl(url);
            config.setUsername(databaseSettings.getUsername());
            config.setPassword(databaseSettings.getPassword());

            connectionSource = type != DatabaseType.SQLITE
                    ? new DataSourceConnectionSource(new HikariDataSource(config), url)
                    : new JdbcConnectionSource(url);
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
            TableUtils.createTableIfNotExists(connectionSource, ProtectedPlayer.class);
            migrate();
        } catch (Throwable e) {
            if (databaseSettings.isStopServerOnFailedConnection()) {
                logger.error("Database is not connected. Server stopping...");
                logger.error("error message: " + e.getMessage());
                plugin.getProxy().shutdown();
            }
            return;
        }

        if (connectionSource == null && databaseSettings.isStopServerOnFailedConnection()) {
            logger.error("Database is not connected. Server stopping...");
            plugin.getProxy().shutdown();
        }
    }

    private void migrate() throws SQLException {
        if (!isColumnExists("vkontakte")) {
            protectedPlayerDao.executeRaw("ALTER TABLE `auth_players` ADD COLUMN vkontakte INTEGER DEFAULT 0;");
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
        }
        if (!isColumnExists("vkontakte_2fa")) {
            protectedPlayerDao.executeRaw("ALTER TABLE `auth_players` ADD COLUMN vkontakte_2fa BOOLEAN DEFAULT true;");
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
        }
        if (!isColumnExists("online_uuid")) {
            protectedPlayerDao.executeRaw("ALTER TABLE `auth_players` ADD COLUMN online_uuid CHAR(36) default NULL;");
            protectedPlayerDao = new BaseProtectPlayerDao(plugin, connectionSource);
        }
    }

    private boolean isColumnExists(String columnName) {
        return protectedPlayerDao.getTableInfo().hasColumnName(columnName);
    }

    private void loadDatabaseLibrary(DatabaseType databaseType) {
        final LibraryProvider libraryLoader = plugin.getLibraryProvider();

        switch (databaseType) {
            case SQLITE -> libraryLoader.loadLibrary(Libraries.SQLITE);
            case MARIADB -> libraryLoader.loadLibrary(Libraries.MARIADB);
            case POSTGRESQL -> libraryLoader.loadLibrary(Libraries.POSTGRESQL);
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
