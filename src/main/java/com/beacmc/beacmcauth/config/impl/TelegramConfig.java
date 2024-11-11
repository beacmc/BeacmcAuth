package com.beacmc.beacmcauth.config.impl;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.config.ConfigLoader;
import com.beacmc.beacmcauth.config.ConfigValue;
import net.dv8tion.jda.api.entities.Activity;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TelegramConfig {


    @ConfigValue(key = "token")
    private String token;

    @ConfigValue(key = "enable")
    private boolean enabled;

    @ConfigValue(key = "disable-unlink")
    private boolean disableUnlink;

    @ConfigValue(key = "code-length")
    private Integer codeLength;

    @ConfigValue(key = "reset-password-length")
    private Integer passwordResetLength;

    @ConfigValue(key = "time-per-confirm")
    private Integer timePerConfirm;

    @ConfigValue(key = "max-link")
    private Integer maxLink;

    @ConfigValue(key = "link-command")
    private String linkCommand;

    @ConfigValue(key = "accounts-command")
    private String accountsCommand;

    @ConfigValue(key = "code-chars")
    private String codeChars;

    @ConfigValue(key = "reset-password-chars")
    private String resetPasswordChars;

    private Configuration config;

    public TelegramConfig(ConfigLoader loader) {
        File file = new File(BeacmcAuth.getInstance().getDataFolder(), "telegram.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            loader.loadConfig(file, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String messagePath, Map<String, String> placeholders) {
        final Configuration messages = config.getSection("messages");
        String message = messages.getString(messagePath);
        if (placeholders == null) return message;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }
        return message;
    }

    public String getMessage(String messagePath) {
        return getMessage(messagePath, null);
    }

    public String getResetPasswordChars() {
        return resetPasswordChars;
    }

    public Integer getCodeLength() {
        return codeLength;
    }

    public Integer getMaxLink() {
        return maxLink;
    }

    public Integer getTimePerConfirm() {
        return timePerConfirm;
    }

    public String getToken() {
        return token;
    }

    public String getCodeChars() {
        return codeChars;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getAccountsCommand() {
        return accountsCommand;
    }

    public boolean isDisableUnlink() {
        return disableUnlink;
    }

    public Integer getPasswordResetLength() {
        return passwordResetLength;
    }

    public String getLinkCommand() {
        return linkCommand;
    }
}


