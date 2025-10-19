package com.beacmc.beacmcauth.api.config.social;

import com.beacmc.beacmcauth.api.config.social.settings.KeyboardsSettings;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.vk.api.sdk.objects.newsfeed.ListFull;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import javax.swing.*;
import java.util.List;

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

    int getMessageSendDelaySeconds();

    Integer getPasswordResetLength();

    String getLinkCommand();

    KeyboardsSettings getKeyboards();

    SocialMessages getMessages();
}
