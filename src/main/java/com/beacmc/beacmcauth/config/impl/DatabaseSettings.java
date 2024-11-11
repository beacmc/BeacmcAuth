package com.beacmc.beacmcauth.config.impl;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.config.ConfigLoader;
import com.beacmc.beacmcauth.config.ConfigValue;
import com.beacmc.beacmcauth.database.DatabaseType;

import java.io.File;

public class DatabaseSettings {

    @ConfigValue(key = "database.type")
    private DatabaseType type;

    @ConfigValue(key = "database.host")
    private String host;

    @ConfigValue(key = "database.database")
    private String database;

    @ConfigValue(key = "database.username")
    private String username;

    @ConfigValue(key = "database.password")
    private String password;

    @ConfigValue(key = "database.stop-server-on-failed-connection")
    private boolean stopServerOnFailedConnection;

    private String url;

    public DatabaseSettings(ConfigLoader loader, File file) {
        loader.loadConfig(file, this);
        if (type.name().toLowerCase().equals("sqlite")) {
            url = "jdbc:sqlite:" + BeacmcAuth.getInstance().getDataFolder().getAbsolutePath() + "/auth.db";
        } else {
            url = "jdbc:" + type.name().toLowerCase() + "://" + host + "/" + database;
        }
    }

    public boolean isStopServerOnFailedConnection() {
        return stopServerOnFailedConnection;
    }

    public String getUrl() {
        return url;
    }

    public DatabaseType getType() {
        return type;
    }

    public String getDatabase() {
        return database;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
