package com.beacmc.beacmcauth.api.model;

import com.beacmc.beacmcauth.api.cache.CachedData;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private @NotNull String lowercaseName;

    @DatabaseField(columnName = "real_name", canBeNull = false)
    private @NotNull String realName;

    @DatabaseField(columnName = "uuid", id = true, canBeNull = false)
    private @NotNull UUID uuid;

    @DatabaseField(columnName = "online_uuid")
    private @Nullable UUID onlineUuid;

    @DatabaseField(columnName = "password")
    private @Nullable String password;

    @DatabaseField(columnName = "session", defaultValue = "0")
    private long session;

    @DatabaseField(columnName = "last_join", defaultValue = "0")
    private long lastJoin;

    @DatabaseField(columnName = "banned")
    private boolean banned;

    @DatabaseField(columnName = "discord_2fa", defaultValue = "true")
    private boolean discordTwoFaEnabled;

    @DatabaseField(columnName = "telegram_2fa", defaultValue = "true")
    private boolean telegramTwoFaEnabled;

    @DatabaseField(columnName = "vkontakte_2fa", defaultValue = "true")
    private boolean vkontakteTwoFaEnabled;

    @DatabaseField(columnName = "reg_ip")
    private @Nullable String registerIp;

    @DatabaseField(columnName = "last_ip")
    private @Nullable String lastIp;

    @DatabaseField(columnName = "email")
    private @Nullable String email;

    @Setter(AccessLevel.NONE)
    @DatabaseField(columnName = "secret_question")
    private @Nullable String secretQuestion;

    @Setter(AccessLevel.NONE)
    @DatabaseField(columnName = "hashed_secret_answer")
    private @Nullable String hashedSecretAnswer;

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

    public ProtectedPlayer setSecretQuestion(@Nullable String question, @Nullable String answer) {
        this.secretQuestion = question;
        this.hashedSecretAnswer = answer != null
                ? BCrypt.hashpw(answer, BCrypt.gensalt(10))
                : null;
        return this;
    }

    public @NotNull UUID getAdaptiveUuid() {
        return onlineUuid != null ? onlineUuid : uuid;
    }

    public boolean checkSecretAnswer(String answer) {
        return secretQuestion != null
                && hashedSecretAnswer != null
                && BCrypt.checkpw(answer, hashedSecretAnswer);
    }

    public boolean isValidIp(String ip) {
        return lastIp != null && lastIp.equals(ip);
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
