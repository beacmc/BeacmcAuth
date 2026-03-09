package com.beacmc.beacmcauth.core.database;

import com.beacmc.beacmcauth.api.database.Database;

import java.util.Map;

class DatabaseVersionMigrator {

    transient Map<String, String> migrateColumns = Map.of(
            "vkontakte", "ALTER TABLE `auth_players` ADD COLUMN vkontakte INTEGER DEFAULT 0;",
            "vkontakte_2fa", "ALTER TABLE `auth_players` ADD COLUMN vkontakte_2fa BOOLEAN DEFAULT true;",
            "online_uuid", "ALTER TABLE `auth_players` ADD COLUMN online_uuid CHAR(36) default NULL;",
            "email", "ALTER TABLE `auth_players` ADD COLUMN email CHAR(255) default NULL;",
            "secret_question", "ALTER TABLE `auth_players` ADD COLUMN secret_question CHAR(255) default NULL;",
            "hashed_secret_answer", "ALTER TABLE `auth_players` ADD COLUMN hashed_secret_answer CHAR(255) default NULL;"
    );

    DatabaseVersionMigrator(Database database) throws Exception {
        try {
            for (Map.Entry<String, String> entry : migrateColumns.entrySet()) {
                String columnName = entry.getKey();
                String request = entry.getValue();

                if (!database.isColumnExists(columnName)) {
                    database.getProtectedPlayerDao().executeRaw(request);
                }
            }
        } finally {
            database.updateDao();
        }
    }
}
