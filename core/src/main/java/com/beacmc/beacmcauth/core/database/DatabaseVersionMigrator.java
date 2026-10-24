package com.beacmc.beacmcauth.core.database;

import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;

import java.util.Map;

class DatabaseVersionMigrator {

    private static final Map<String, String> migrateColumns = Map.of(
            "online_uuid",
            "ALTER TABLE %s ADD COLUMN %s CHAR(36) DEFAULT NULL".formatted(
                    ProtectedPlayer.TABLE_NAME,
                    ProtectedPlayer.ONLINE_UUID_COLUMN),

            "hash_type",
            "ALTER TABLE %s ADD COLUMN %s VARCHAR(32) DEFAULT NULL".formatted(
                    ProtectedPlayer.TABLE_NAME,
                    ProtectedPlayer.HASH_TYPE_COLUMN)
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
                                "UPDATE %s SET hash_type = 'bcrypt_2a' WHERE %s IS NULL".formatted(
                                        ProtectedPlayer.TABLE_NAME,
                                        ProtectedPlayer.HASH_TYPE_COLUMN)
                        );
                    }
                }
            }
        } finally {
            database.updateDao();
        }
    }
}
