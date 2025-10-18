package com.beacmc.beacmcauth.api.config.social.settings;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;

import java.util.List;

public interface KeyboardsSettings {

    Keyboard createAccountManageKeyboard(ProtectedPlayer protectedPlayer);

    Keyboard createAccountsListKeyboard(List<ProtectedPlayer> protectedPlayers);
}
