package com.beacmc.beacmcauth.api.config.social;

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

    SocialMessages getMessages();
}
