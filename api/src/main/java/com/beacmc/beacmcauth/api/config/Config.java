package com.beacmc.beacmcauth.api.config;

import com.beacmc.beacmcauth.api.server.Server;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public interface Config {

    Server findServer(List<String> configServers);

    List<String> getDisabledServers();

    boolean isNameCaseControl();

    Integer getBCryptRounds();

    Pattern getNicknameRegex();

    Pattern getPasswordRegex();

    String getLinkCommand();

    boolean isNbsSongSupport();

    String getRecoveryPasswordChars();

    List<String> getWhitelistCommands();

    Integer getPasswordAttempts();

    DatabaseSettings getDatabaseSettings();

    int getRegisterMessageSendDelaySeconds();

    int getLoginMessageSendDelaySeconds();

    Integer getSessionTime();

    Integer getTimePerLogin();

    Integer getTimePerRegister();

    boolean isRegisterRepeatPassword();

    List<String> getAuthServers();

    List<String> getLobbyServers();

    boolean isDebugEnabled();

    TimeUnit getPremiumCacheTimeUnit();

    long getPremiumCacheTimeUnitValue();

    long getLifetimeOfTemporaryPremiumVerificationTimeUnitValue();

    TimeUnit getLifetimeOfTemporaryPremiumVerificationTimeUnit();

    AccountLimiterSettings getAccountLimiterSettings();

    ConfigMessages getMessages();
}
