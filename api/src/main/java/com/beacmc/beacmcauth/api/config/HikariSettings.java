package com.beacmc.beacmcauth.api.config;

import com.zaxxer.hikari.HikariConfig;

import java.util.Map;

public interface HikariSettings {

    int getMaximumPoolSize();

    int getMinimumIdle();

    long getConnectionTimeout();

    Map<String, String> getDataSourceProperties();

    default HikariConfig createHikariInstance() {
        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(getConnectionTimeout());
        config.setMinimumIdle(getMinimumIdle());
        config.setMaximumPoolSize(getMaximumPoolSize());
        for (Map.Entry<String, String> entry : getDataSourceProperties().entrySet()) {
            config.addDataSourceProperty(entry.getKey(), entry.getValue());
        }
        return config;
    }
}
