package com.beacmc.beacmcauth.api.config;

import com.beacmc.beacmcauth.api.server.Server;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface Config {

    boolean isAzLinkIntegration();

    Server findServer(List<String> configServers);

    String getMessage(String path, Map<String, String> placeholders);

    default String getMessage(String path) {
        return getMessage(path, null);
    }

    List<String> getDisabledServers();

    boolean isNameCaseControl();

    Integer getBCryptRounds();

    Pattern getNicknameRegex();

    String getLinkCommand();

    List<String> getWhitelistCommands();

    Integer getPasswordAttempts();

    Integer getPasswordMaxLength();

    DatabaseSettings getDatabaseSettings();

    Integer getPasswordMinLength();

    Integer getSessionTime();

    Integer getTimePerLogin();

    Integer getTimePerRegister();

    List<String> getAuthServers();

    List<String> getGameServers();

    boolean isDebugEnabled();
}
