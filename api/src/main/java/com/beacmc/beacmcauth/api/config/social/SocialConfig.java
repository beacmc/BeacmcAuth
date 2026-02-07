package com.beacmc.beacmcauth.api.config.social;

import com.beacmc.beacmcauth.api.config.social.settings.KeyboardsSettings;

public interface SocialConfig {

    String getResetPasswordChars();

    Integer getCodeLength();

    Integer getMaxLink();

    Integer getTimePerConfirm();

    String getCodeChars();

    boolean isEnabled();

    String getAccountsCommand();

    boolean isDisableUnlink();

    int getMessageSendDelaySeconds();

    Integer getPasswordResetLength();

    String getLinkCommand();

    KeyboardsSettings getKeyboards();

    SocialMessages getMessages();
}
