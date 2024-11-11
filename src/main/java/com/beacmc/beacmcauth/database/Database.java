package com.beacmc.beacmcauth.database;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.DatabaseSettings;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.database.dao.impl.ProtectedPlayerDaoImpl;
import com.beacmc.beacmcauth.lib.Libraries;
import com.beacmc.beacmcauth.lib.LibraryLoader;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.md_5.bungee.api.event.ServerConnectEvent;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class Database {

    private ConnectionSource connectionSource;
    private ProtectedPlayerDao protectedPlayerDao;

    public void connect() {
        DatabaseSettings database = BeacmcAuth.getConfig().getDatabaseSettings();
        assert connectionSource == null;
        try {
            loadDatabaseLibrary(database.getType().name().toLowerCase());
            connectionSource = new JdbcConnectionSource(database.getUrl(), database.getUsername(), database.getPassword());
            protectedPlayerDao = new ProtectedPlayerDaoImpl(connectionSource);
            TableUtils.createTableIfNotExists(connectionSource, ProtectedPlayer.class);
        } catch (Throwable e) {
            if (database.isStopServerOnFailedConnection()) {
                Logger logger = BeacmcAuth.getInstance().getLogger();
                logger.severe("Database is not connected. Server stopping...");
                logger.severe("error message: " + e.getMessage());
                BeacmcAuth.getInstance().getProxy().stop();
            }
        }

        if (connectionSource == null && database.isStopServerOnFailedConnection()) {
            Logger logger = BeacmcAuth.getInstance().getLogger();
            logger.severe("Database is not connected. Server stopping...");
            BeacmcAuth.getInstance().getProxy().stop();
        }
    }

    private void loadDatabaseLibrary(String databaseType) {
        final LibraryLoader libraryLoader = BeacmcAuth.getLibraryLoader();

        switch (databaseType) {
            case "sqlite": libraryLoader.loadLibrary(Libraries.SQLITE); break;
            case "mariadb": libraryLoader.loadLibrary(Libraries.MARIADB); break;
            case "postgresql": libraryLoader.loadLibrary(Libraries.POSTGRESQL); break;
        }
    }

    public ProtectedPlayerDao getProtectedPlayerDao() {
        return protectedPlayerDao;
    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }
}
