package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.config.HikariSettings;
import com.beacmc.beacmcauth.api.database.DatabaseType;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

import java.util.Map;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseDatabaseSettings implements DatabaseSettings {

    @Comment("Types: SQLITE, MYSQL, MARIADB, POSTGRESQL")
    private DatabaseType type = DatabaseType.SQLITE;
    private String host = "localhost:3306";
    private String database = "database_name";
    private String username = "username";
    private String password = "password";
    @Comment({"", "Disabling this feature is strongly discouraged.", " In the event of a failed connection to the database, players will be able to access the server without a password!"})
    private boolean stopServerOnFailedConnection = true;
    private HikariConfig hikariSettings = new HikariConfig();

    @Override
    public String getUrl(BeacmcAuth plugin) {
        return type == DatabaseType.SQLITE
                ? "jdbc:sqlite:%s/auth.db".formatted(plugin.getDataFolder().getAbsolutePath())
                : "jdbc:%s://%s/%s".formatted(type.name().toLowerCase(), host, database);
    }

    @Getter
    @Configuration
    public static class HikariConfig implements HikariSettings {

        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeout = 30_000;
        private Map<String, String> dataSourceProperties = Map.of(
                "tcpKeepAlive", "true",
                "useServerPrepStmts", "true"
        );
    }
}
