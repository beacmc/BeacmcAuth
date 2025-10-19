package com.beacmc.beacmcauth.api.config.social.settings;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface KeyboardsSettings {

    Keyboard createAccountManageKeyboard(ProtectedPlayer protectedPlayer);

    Keyboard createConfirmationKeyboard(ProtectedPlayer protectedPlayer);

    @Nullable Keyboard createAccountsListKeyboard(List<ProtectedPlayer> protectedPlayers, int page);

    AccountListKeyboardSettings getAccountListKeyboardSettings();

    interface AccountListKeyboardSettings {

        int getAccountsInOnePage();

        Button getAccountButton();

        List<Button> getHeaderButtons();

        List<Button> getFooterButtons();
    }
}
