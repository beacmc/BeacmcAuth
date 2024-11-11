package com.beacmc.beacmcauth.config.impl;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.config.ConfigLoader;
import com.beacmc.beacmcauth.config.ConfigValue;
import com.beacmc.beacmcauth.util.Color;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BaseConfig {

    @ConfigValue(key = "session-time")
    private Integer sessionTime;

    @ConfigValue(key = "time-per-login")
    private Integer timePerLogin;

    @ConfigValue(key = "time-per-register")
    private Integer timePerRegister;

    @ConfigValue(key = "bcrypt-rounds")
    private Integer bcryptRounds;

    @ConfigValue(key = "password-min-length")
    private Integer passwordMinLength;

    @ConfigValue(key = "password-max-length")
    private Integer passwordMaxLength;

    @ConfigValue(key = "password-attempts")
    private Integer passwordAttempts;

    @ConfigValue(key = "auth-servers")
    private List<String> authServers;

    @ConfigValue(key = "link-command")
    private String linkCommand;

    @ConfigValue(key = "disabled-servers")
    private List<String> disabledServers;

    @ConfigValue(key = "game-servers")
    private List<String> gameServers;

    @ConfigValue(key = "whitelist-commands")
    private List<String> whitelistCommands;

    @ConfigValue(key = "name-case-control")
    private boolean nameCaseControl;

    @ConfigValue(key = "nickname-regex")
    private Pattern nicknameRegex;

    private DatabaseSettings databaseSettings;
    private Configuration config;

    public BaseConfig(ConfigLoader loader) {
        File file = new File(BeacmcAuth.getInstance().getDataFolder(), "config.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            loader.loadConfig(file, this);
            databaseSettings = new DatabaseSettings(loader, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BaseComponent[] getMessage(String messagePath, Map<String, String> placeholders) {
        final Configuration messages = config.getSection("messages");
        String message = messages.getString(messagePath);
        if (placeholders == null) return Color.of(message);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }
        return Color.of(message);
    }

    public BaseComponent[] getMessage(String messagePath) {
        return getMessage(messagePath, null);
    }

    public ServerInfo findServerInfo(List<String> configServers) {
        for (String configServer : configServers) {
            String[] args = configServer.split(":");
            ServerInfo serverInfo = BeacmcAuth.getInstance().getProxy().getServerInfo(args[0]);
            if (serverInfo == null) {
                BeacmcAuth.getInstance().getLogger().severe("Server " + args[0] + " not found");
                continue;
            }
            int maxPlayers = args.length >= 2 ? Integer.parseInt(args[1]) : 20;
            int players = serverInfo.getPlayers().size();

            if (players < maxPlayers) {
                return serverInfo;
            }
        }
        return null;
    }

    public List<String> getDisabledServers() {
        return disabledServers;
    }

    public boolean isNameCaseControl() {
        return nameCaseControl;
    }

    public Integer getBCryptRounds() {
        return bcryptRounds;
    }

    public Pattern getNicknameRegex() {
        return nicknameRegex;
    }

    public String getLinkCommand() {
        return linkCommand;
    }

    public List<String> getWhitelistCommands() {
        return whitelistCommands;
    }

    public Integer getPasswordAttempts() {
        return passwordAttempts;
    }

    public Integer getPasswordMaxLength() {
        return passwordMaxLength;
    }

    public DatabaseSettings getDatabaseSettings() {
        return databaseSettings;
    }

    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }

    public Integer getSessionTime() {
        return sessionTime;
    }

    public Integer getTimePerLogin() {
        return timePerLogin;
    }

    public Integer getTimePerRegister() {
        return timePerRegister;
    }

    public List<String> getAuthServers() {
        return authServers;
    }

    public List<String> getGameServers() {
        return gameServers;
    }

}
