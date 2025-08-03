package com.beacmc.beacmcauth.api.config.social;

import java.util.Map;

public interface SocialConfig {

    String getResetPasswordChars();

    Integer getCodeLength();

    Integer getMaxLink();

    Integer getTimePerConfirm();

    String getToken();

    String getCodeChars();

    boolean isEnabled();

    String getAccountsCommand();

    boolean isDisableUnlink();

    Integer getPasswordResetLength();

    String getLinkCommand();

    String getMessage(String messagePath, Map<String, String> placeholders);

    default String getMessage(String messagePath) {
        return getMessage(messagePath, null);
    }
}
