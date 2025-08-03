package com.beacmc.beacmcauth.core.config.social;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.api.config.loader.ConfigValue;
import com.beacmc.beacmcauth.api.config.social.VkontakteConfig;
import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Getter
public class BaseVkontakteConfig implements VkontakteConfig {

    private final String token = null;

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

    public BaseVkontakteConfig(BeacmcAuth plugin, ConfigLoader loader) {
        plugin.saveResource("vkontakte.yml");
        File file = new File(plugin.getDataFolder(), "vkontakte.yml");
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
}
