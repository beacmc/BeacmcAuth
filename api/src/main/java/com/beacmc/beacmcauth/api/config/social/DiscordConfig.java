package com.beacmc.beacmcauth.api.config.social;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.config.Configuration;

public interface DiscordConfig extends SocialConfig {

    Long getGuildID();

    Activity.ActivityType getActivityType();

    boolean isActivityEnabled();

    String getActivityText();

    String getActivityUrl();

}
