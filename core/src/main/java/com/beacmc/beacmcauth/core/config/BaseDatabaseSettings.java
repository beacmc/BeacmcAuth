package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.api.config.loader.ConfigValue;
import com.beacmc.beacmcauth.api.database.DatabaseType;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.Ignore;
import lombok.Getter;

import java.io.File;
import java.sql.SQLClientInfoException;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseDatabaseSettings implements DatabaseSettings {

    private DatabaseType type = DatabaseType.SQLITE;
    private String host = "localhost:3306";
    private String database = "database_name";
    private String username = "username";
    private String password = "password";
    private boolean stopServerOnFailedConnection = true;

    @Override
    public String getUrl(BeacmcAuth plugin) {
        return type == DatabaseType.SQLITE
                ? "jdbc:sqlite:%s/auth.db".formatted(plugin.getDataFolder().getAbsolutePath())
                : "jdbc:%s://%s/%s".formatted(type.name().toLowerCase(), host, database);
    }
}
