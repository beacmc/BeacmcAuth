package com.beacmc.beacmcauth.core.database;

import com.beacmc.beacmcauth.api.database.Database;

import java.util.Map;

class DatabaseVersionMigrator {

    private static final Map<String, String> migrateColumns = Map.of(
            "online_uuid",
            "ALTER TABLE auth_players ADD COLUMN online_uuid CHAR(36) DEFAULT NULL",

            "hash_type",
            "ALTER TABLE auth_players ADD COLUMN hash_type VARCHAR(32) DEFAULT NULL"
    );

    DatabaseVersionMigrator(Database database) throws Exception {
        try {
            for (Map.Entry<String, String> entry : migrateColumns.entrySet()) {
                String columnName = entry.getKey();
                String request = entry.getValue();

                if (!database.isColumnExists(columnName)) {
                    database.getProtectedPlayerDao().executeRaw(request);

                    if (columnName.equals("hash_type")) {
                        database.getProtectedPlayerDao().executeRaw(
                                "UPDATE auth_players SET hash_type = 'bcrypt_2a' WHERE hash_type IS NULL"
                        );
                    }
                }
            }
        } finally {
            database.updateDao();
        }
    }
}
