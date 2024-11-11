package com.beacmc.beacmcauth;

import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    @DatabaseField(columnName = "reg_ip")
    private String registerIp;

    @DatabaseField(columnName = "last_ip")
    private String lastIp;

    @DatabaseField(columnName = "discord", defaultValue = "0")
    private long discord;

    @DatabaseField(columnName = "telegram", defaultValue = "0")
    private long telegram;

    private final BeacmcAuth plugin;
    private String linkCode;

    public ProtectedPlayer() {
        plugin = BeacmcAuth.getInstance();
    }

    public ProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, boolean banned, boolean discordTwoFaEnabled, boolean telegramTwoFaEnabled, String registerIp, String lastIp, UUID uuid) {
        plugin = BeacmcAuth.getInstance();
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
        this.linkCode = null;
    }

    public ProxiedPlayer getPlayer() {
        return plugin.getProxy().getPlayer(lowercaseName);
    }

    public static CompletableFuture<ProtectedPlayer> get(String name) {
        final ProtectedPlayerDao dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return dao.queryForId(name.toLowerCase());
            } catch (SQLException ignored) {
            }
            return null;
        });
    }

    public static CompletableFuture<ProtectedPlayer> create(String lowercaseName, String realName, String password, long session, long lastJoin, boolean banned, boolean discordTwoFaEnabled, boolean telegramTwoFaEnabled, String registerIp, String lastIp, UUID uuid) {
        final ProtectedPlayerDao dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();

        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer player = new ProtectedPlayer(lowercaseName, realName, password, session, lastJoin, banned, discordTwoFaEnabled, telegramTwoFaEnabled, registerIp, lastIp, uuid);
                dao.createOrUpdate(player);
                return player;
            } catch (SQLException ignored) {
            }
            return null;
        });
    }

    public CompletableFuture<Void> performLogin() {
        final ProtectedPlayerDao dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();

        return CompletableFuture.supplyAsync(() -> {
            if (getPlayer() == null) {
                return null;
            }

            try {
                String ip = getPlayer().getAddress().getHostName();
                long currentTime = System.currentTimeMillis();
                dao.createOrUpdate(setLastIp(ip).setSession(currentTime).setLastJoin(currentTime));
            } catch (SQLException ignored) {
            }
            return null;
        });
    }

    public CompletableFuture<String> register(String password) {
        final ProtectedPlayerDao dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
        final BaseConfig config = BeacmcAuth.getConfig();

        return CompletableFuture.supplyAsync(() -> {
            try {
                dao.createOrUpdate(this.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(config.getBCryptRounds()))));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public boolean isValidIp(String ip) {
        return lastIp.equals(ip);
    }

    public boolean isSessionActive() {
        final BaseConfig config = BeacmcAuth.getConfig();
        final long currentTimeMillis = System.currentTimeMillis();
        final long sessionTimeMillis = (config.getSessionTime() * 60) * 1000;

        return this.session + sessionTimeMillis > currentTimeMillis;
    }

    public boolean checkPassword(String pass) {
        return BCrypt.checkpw(pass, this.password);
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

    public String getLinkCode() {
        return linkCode;
    }

    public ProtectedPlayer setLinkCode(String linkCode) {
        this.linkCode = linkCode;
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

    public ProtectedPlayer setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
        return this;
    }

    public ProtectedPlayer setSession(long session) {
        this.session = session;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return getLowercaseName().equals(((String) obj).toLowerCase());
        }
        return super.equals(obj);
    }
}
