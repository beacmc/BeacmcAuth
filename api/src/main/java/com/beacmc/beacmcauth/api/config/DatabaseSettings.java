package com.beacmc.beacmcauth.api.config;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.database.DatabaseType;

public interface DatabaseSettings {

    boolean isStopServerOnFailedConnection();

    String getUrl(BeacmcAuth plugin);

    DatabaseType getType();

    String getDatabase();

    String getHost();

    String getUsername();

    String getPassword();

    HikariSettings getHikariSettings();
}
