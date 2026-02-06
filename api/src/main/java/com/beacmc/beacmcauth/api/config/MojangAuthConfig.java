package com.beacmc.beacmcauth.api.config;

public interface MojangAuthConfig {

    String getAuthUrl();

    int getUserFoundCode();

    int getRateLimitedCode();

    boolean isLicenseJoinRateLimited();

    boolean isLicenseJoinMojangDown();

    String getUniqueIdField();

    String getRateLimitRetryAfterField();
}
