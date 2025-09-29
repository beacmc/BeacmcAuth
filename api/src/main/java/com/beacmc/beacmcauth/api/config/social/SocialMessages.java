package com.beacmc.beacmcauth.api.config.social;

public interface SocialMessages {

    String getStartMessage();

    String getConfirmationMessage();

    String getConfirmationButtonAcceptText();

    String getConfirmationButtonDeclineText();

    String getNoConfirmation();

    String getConfirmationSuccess();

    String getConfirmationDenied();

    String getPlayerOffline();

    String getPrivateChannelOnly();

    String getLinkCommandUsage();

    String getLinkCommandPlayerOffline();

    String getLinkCommandPlayerNotFound();

    String getLinkCommandAlreadyLinked();

    String getLinkLimit();

    String getLinkMessage();

    String getChooseAccount();

    String getAccountInfo();

    String getAccountResetPasswordButton();

    String getAccountResetPassword();

    String getAccountUnlinkButton();

    String getCooldown();

    String getAccountAlreadyUnlink();

    String getAccountUnlinkSuccess();

    String getUnlinkDisabled();

    String getAccountNotLinked();

    String getPlayerInfoOnline();

    String getPlayerInfoOffline();

    String getAccountKickButton();

    String getAccountKickSuccess();

    String getAccount2faToggleButton();

    String getAccount2faEnabled();

    String getAccount2faDisabled();

    String getAccountBanToggleButton();

    String getAccountBanned();

    String getAccountUnbanned();

    String getAccountsEmpty();
}
