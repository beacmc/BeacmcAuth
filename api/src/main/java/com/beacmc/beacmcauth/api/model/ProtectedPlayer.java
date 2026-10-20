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
@DatabaseTable(tableName = ProtectedPlayer.TABLE_NAME)
public class ProtectedPlayer implements CachedData<UUID> {

    public static final String TABLE_NAME = "auth_players";
    public static final String LOWERCASE_NAME_COLUMN = "lowercase_name";
    public static final String REAL_NAME_COLUMN = "real_name";
    public static final String UUID_COLUMN = "uuid";
    public static final String ONLINE_UUID_COLUMN = "online_uuid";
    public static final String PASSWORD_COLUMN = "password";
    public static final String SESSION_COLUMN = "session";
    public static final String LAST_JOIN_COLUMN = "last_join";
    public static final String BANNED_COLUMN = "banned";
    public static final String DISCORD_2FA_COLUMN = "discord_2fa";
    public static final String TELEGRAM_2FA_COLUMN = "telegram_2fa";
    public static final String REGISTER_IP_COLUMN = "reg_ip";
    public static final String LAST_IP_COLUMN = "last_ip";
    public static final String EMAIL_COLUMN = "email";
    public static final String SECRET_QUESTION_COLUMN = "secret_question";
    public static final String SECRET_ANSWER_COLUMN = "hashed_secret_answer";
    public static final String DISCORD_COLUMN = "discord";
    public static final String TELEGRAM_COLUMN = "telegram";

    @DatabaseField(columnName = LOWERCASE_NAME_COLUMN, canBeNull = false)
    private @NotNull String lowercaseName;

    @DatabaseField(columnName = REAL_NAME_COLUMN, canBeNull = false)
    private @NotNull String realName;

    @DatabaseField(columnName = UUID_COLUMN, id = true, canBeNull = false)
    private @NotNull UUID uuid;

    @DatabaseField(columnName = ONLINE_UUID_COLUMN)
    private @Nullable UUID onlineUuid;

    @DatabaseField(columnName = PASSWORD_COLUMN)
    private @Nullable String password;

    @DatabaseField(columnName = SESSION_COLUMN, defaultValue = "0")
    private long session;

    @DatabaseField(columnName = LAST_JOIN_COLUMN, defaultValue = "0")
    private long lastJoin;

    @DatabaseField(columnName = BANNED_COLUMN)
    private boolean banned;

    @DatabaseField(columnName = DISCORD_2FA_COLUMN, defaultValue = "true")
    private boolean discordTwoFaEnabled;

    @DatabaseField(columnName = TELEGRAM_2FA_COLUMN, defaultValue = "true")
    private boolean telegramTwoFaEnabled;

    @DatabaseField(columnName = REGISTER_IP_COLUMN)
    private @Nullable String registerIp;

    @DatabaseField(columnName = LAST_IP_COLUMN)
    private @Nullable String lastIp;

    @DatabaseField(columnName = EMAIL_COLUMN)
    private @Nullable String email;

    @Setter(AccessLevel.NONE)
    @DatabaseField(columnName = SECRET_QUESTION_COLUMN)
    private @Nullable String secretQuestion;

    @Setter(AccessLevel.NONE)
    @DatabaseField(columnName = SECRET_ANSWER_COLUMN)
    private @Nullable String hashedSecretAnswer;

    @DatabaseField(columnName = DISCORD_COLUMN, defaultValue = "0")
    private long discord;

    @DatabaseField(columnName = TELEGRAM_COLUMN, defaultValue = "0")
    private long telegram;

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
