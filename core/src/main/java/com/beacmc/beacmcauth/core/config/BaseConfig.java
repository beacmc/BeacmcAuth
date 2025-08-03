package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.api.config.loader.ConfigValue;
import com.beacmc.beacmcauth.api.server.Server;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BaseConfig implements Config {

    @ConfigValue(key = "session-time")
    private Integer sessionTime;

    @ConfigValue(key = "debug")
    private boolean debugEnabled;

    @ConfigValue(key = "azlink-integration")
    private boolean azLinkIntegration;

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
    private final BeacmcAuth plugin;
    public String messageSection = "messages";

    public BaseConfig(BeacmcAuth plugin, ConfigLoader loader) {
        this.plugin = plugin;
        plugin.saveResource("config.yml");
        File file = new File(plugin.getDataFolder(), "config.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            loader.loadConfig(file, this);
            databaseSettings = new BaseDatabaseSettings(plugin, loader, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String messagePath, Map<String, String> placeholders) {
        final Configuration messages = config.getSection(messageSection);
        String message = messages.getString(messagePath);
        if (placeholders == null) return message;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }
        return message;
    }

    @Override
    public Server findServer(List<String> configServers) {
        for (String configServer : configServers) {
            String[] args = configServer.split(":");
            Server serverInfo = plugin.getProxy().getServer(args[0]);
            if (serverInfo == null) {
                plugin.getServerLogger().error("Server " + args[0] + " not found");
                continue;
            }
            int maxPlayers = args.length >= 2 ? Integer.parseInt(args[1]) : 20;
            int players = serverInfo.getOnlinePlayersSize();

            if (players < maxPlayers) {
                return serverInfo;
            }
        }
        return null;
    }

    public boolean isAzLinkIntegration() {
        return azLinkIntegration;
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

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
