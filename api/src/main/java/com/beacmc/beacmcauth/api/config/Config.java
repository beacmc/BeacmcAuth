package com.beacmc.beacmcauth.api.config;

import com.beacmc.beacmcauth.api.server.Server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public interface Config {

    boolean isAzLinkIntegration();

    Server findServer(List<String> configServers);

    List<String> getDisabledServers();

    boolean isNameCaseControl();

    Integer getBCryptRounds();

    Pattern getNicknameRegex();

    String getLinkCommand();

    List<String> getWhitelistCommands();

    Integer getPasswordAttempts();

    Integer getPasswordMaxLength();

    DatabaseSettings getDatabaseSettings();

    int getRegisterMessageSendDelaySeconds();

    int getLoginMessageSendDelaySeconds();

    Integer getPasswordMinLength();

    Integer getSessionTime();

    Integer getTimePerLogin();

    Integer getTimePerRegister();

    List<String> getAuthServers();

    List<String> getLobbyServers();

    boolean isDebugEnabled();

    TimeUnit getPremiumCacheTimeUnit();

    long getPremiumCacheTimeUnitValue();

    long getLifetimeOfTemporaryPremiumVerificationTimeUnitValue();

    TimeUnit getLifetimeOfTemporaryPremiumVerificationTimeUnit();

    ConfigMessages getMessages();
}
