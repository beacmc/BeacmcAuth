package com.beacmc.beacmcauth.api;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.EqualsAndHashCode;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

@EqualsAndHashCode
@DatabaseTable(tableName = "auth_players")
public class ProtectedPlayer {

    @DatabaseField(columnName = "lowercase_name", id = true, canBeNull = false)
    private String lowercaseName;

    @DatabaseField(columnName = "real_name", canBeNull = false)
    private String realName;

    @DatabaseField(columnName = "uuid")
    private UUID uuid;

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

    public ProtectedPlayer() {}

    public ProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, boolean banned, boolean discordTwoFaEnabled, boolean telegramTwoFaEnabled, String registerIp, String lastIp, UUID uuid) {
        setLowercaseName(lowercaseName);
        setRealName(realName);
        setBanned(banned);
        setDiscordTwoFaEnabled(discordTwoFaEnabled);
        setTelegramTwoFaEnabled(telegramTwoFaEnabled);
        setSession(session);
        setLastIp(lastIp);
        setRegisterIp(registerIp);
        setLastJoin(lastJoin);
        setPassword(password);
        setUUID(uuid);
    }

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

    public ProtectedPlayer setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
        return this;
    }

    public UUID getUUID() {
        return uuid;
    }

    public ProtectedPlayer setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public boolean isTelegramTwoFaEnabled() {
        return telegramTwoFaEnabled;
    }

    public ProtectedPlayer setTelegramTwoFaEnabled(boolean telegramTwoFaEnabled) {
        this.telegramTwoFaEnabled = telegramTwoFaEnabled;
        return this;
    }

    public boolean isVkontakteTwoFaEnabled() {
        return vkontakteTwoFaEnabled;
    }

    public ProtectedPlayer setVkontakteTwoFaEnabled(boolean vkontakteTwoFaEnabled) {
        this.vkontakteTwoFaEnabled = vkontakteTwoFaEnabled;
        return this;
    }

    public long getTelegram() {
        return telegram;
    }

    public ProtectedPlayer setTelegram(long telegram) {
        this.telegram = telegram;
        return this;
    }

    public long getSession() {
        return session;
    }

    public boolean isDiscordTwoFaEnabled() {
        return discordTwoFaEnabled;
    }

    public ProtectedPlayer setDiscordTwoFaEnabled(boolean discordTwoFaEnabled) {
        this.discordTwoFaEnabled = discordTwoFaEnabled;
        return this;
    }
    public long getLastJoin() {
        return lastJoin;
    }

    public String getLowercaseName() {
        return lowercaseName;
    }

    public boolean isBanned() {
        return banned;
    }

    public String getPassword() {
        return password;
    }

    public String getLastIp() {
        return lastIp;
    }

    public String getRealName() {
        return realName;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public ProtectedPlayer setPassword(String password) {
        this.password = password;
        return this;
    }

    public long getDiscord() {
        return discord;
    }

    public ProtectedPlayer setDiscord(long discord) {
        this.discord = discord;
        return this;
    }

    public ProtectedPlayer setBanned(boolean banned) {
        this.banned = banned;
        return this;
    }

    public ProtectedPlayer setLastIp(String lastIp) {
        this.lastIp = lastIp;
        return this;
    }

    public ProtectedPlayer setLowercaseName(String lowercaseName) {
        this.lowercaseName = lowercaseName.toLowerCase();
        return this;
    }

    public ProtectedPlayer setRealName(String realName) {
        this.realName = realName;
        return this;
    }

    public Integer getVkontakte() {
        return vkontakte;
    }

    public ProtectedPlayer setVkontakte(Integer vkontakte) {
        this.vkontakte = vkontakte;
        return this;
    }

    public ProtectedPlayer setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
        return this;
    }

    public ProtectedPlayer setSession(long session) {
        this.session = session;
        return this;
    }
}
