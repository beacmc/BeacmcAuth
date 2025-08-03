package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.api.config.loader.ConfigValue;
import com.beacmc.beacmcauth.api.database.DatabaseType;
import lombok.Getter;

import java.io.File;

@Getter
public class BaseDatabaseSettings implements DatabaseSettings {

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

    public BaseDatabaseSettings(BeacmcAuth plugin, ConfigLoader loader, File file) {
        loader.loadConfig(file, this);
        if (type.name().toLowerCase().equals("sqlite")) {
            url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/auth.db";
        } else {
            url = "jdbc:" + type.name().toLowerCase() + "://" + host + "/" + database;
        }
    }
}
