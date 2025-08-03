package com.beacmc.beacmcauth.core.config.social;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.api.config.loader.ConfigValue;
import com.beacmc.beacmcauth.api.config.social.DiscordConfig;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Getter
public class BaseDiscordConfig implements DiscordConfig {

    @ConfigValue(key = "token")
    private String token;

    @ConfigValue(key = "enable")
    private boolean enabled;

    @ConfigValue(key = "disable-unlink")
    private boolean disableUnlink;

    @ConfigValue(key = "guild-id")
    private Long guildID;

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

    @ConfigValue(key = "activity.enable")
    private boolean activityEnabled;

    @ConfigValue(key = "activity.type")
    private String activityType;

    @ConfigValue(key = "activity.text")
    private String activityText;

    @ConfigValue(key = "activity.url")
    private String activityUrl;

    @ConfigValue(key = "code-chars")
    private String codeChars;

    @ConfigValue(key = "reset-password-chars")
    private String resetPasswordChars;

    @ConfigValue(key = "sync-roles")
    private Configuration syncRoles;

    private Configuration config;
    private final BeacmcAuth plugin;

    public BaseDiscordConfig(BeacmcAuth plugin, ConfigLoader loader) {
        this.plugin = plugin;

        plugin.saveResource("discord.yml");
        File file = new File(plugin.getDataFolder(), "discord.yml");
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

    /*public Role getRoleByGroup(String group) {
        Guild guild = ((DiscordProvider) plugin.getDiscordProvider()).getGuild();
        if (group == null || guild == null) return null;

        for (String executePermission : syncRoles.getKeys()) {
            if (executePermission.equals(group)) {
                return guild.getRoleById(syncRoles.getLong(executePermission));
            }
        }
        return null;
    }*/

    @Override
    public Role getRoleByGroup(String group) {
        return null;
    }
}
