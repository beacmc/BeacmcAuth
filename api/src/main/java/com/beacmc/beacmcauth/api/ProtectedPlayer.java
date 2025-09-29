package com.beacmc.beacmcauth.api;

import com.beacmc.beacmcauth.api.cache.CachedData;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import lombok.experimental.Accessors;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

@EqualsAndHashCode
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@DatabaseTable(tableName = "auth_players")
public class ProtectedPlayer implements CachedData<UUID> {

    @DatabaseField(columnName = "lowercase_name", canBeNull = false)
    private String lowercaseName;

    @DatabaseField(columnName = "real_name", canBeNull = false)
    private String realName;

    @DatabaseField(columnName = "uuid", id = true, canBeNull = false)
    private UUID uuid;

    @DatabaseField(columnName = "online_uuid")
    private UUID onlineUuid;

    @DatabaseField(columnName = "password")
    private String password;

    @DatabaseField(columnName = "session", defaultValue = "0")
    private long session;

    @DatabaseField(columnName = "last_join", defaultValue = "0")
    private long lastJoin;

    @DatabaseField(columnName = "banned")
    private boolean banned;

    @DatabaseField(columnName = "discord_2fa")
    private boolean discordTwoFaEnabled;

    @DatabaseField(columnName = "telegram_2fa")
    private boolean telegramTwoFaEnabled;

    @DatabaseField(columnName = "vkontakte_2fa")
    private boolean vkontakteTwoFaEnabled;

    @DatabaseField(columnName = "reg_ip")
    private String registerIp;

    @DatabaseField(columnName = "last_ip")
    private String lastIp;

    @DatabaseField(columnName = "discord", defaultValue = "0")
    private long discord;

    @DatabaseField(columnName = "telegram", defaultValue = "0")
    private long telegram;

    @DatabaseField(columnName = "vkontakte", defaultValue = "0")
    private Integer vkontakte;

    public boolean isSessionActive(long sessionTime) {
        final long currentTimeMillis = System.currentTimeMillis();
        final long sessionTimeMillis = (sessionTime * 60) * 1000;
        
        return getSession() + sessionTimeMillis > currentTimeMillis;
    }

    public boolean isValidIp(String ip) {
        return getLastIp().equals(ip);
    }

    public boolean checkPassword(String pass) {
        return BCrypt.checkpw(pass, getPassword());
    }

    public boolean isRegister() {
        return getPassword() != null;
    }

    @Override
    public UUID getId() {
        return getUuid();
    }
}
