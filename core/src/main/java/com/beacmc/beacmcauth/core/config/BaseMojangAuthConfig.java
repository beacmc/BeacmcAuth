package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.config.MojangAuthConfig;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseMojangAuthConfig implements MojangAuthConfig {

    @Comment("Use %s where the player's name will appear")
    private String authUrl = "https://api.mojang.com/users/profiles/minecraft/%s";
    private int userFoundCode = 200;
    private int rateLimitedCode = 429;
    private boolean licenseJoinRateLimited = true;
    private boolean licenseJoinMojangDown = true;
    private String uniqueIdField = "id";
    private String rateLimitRetryAfterField = "Retry-After";
}
