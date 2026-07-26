package com.beacmc.beacmcauth.api.event;

import com.beacmc.beacmcauth.api.event.type.*;

public interface EventListener {

    default void onLogin(AuthLoginEvent event) {}

    default void onRegister(AuthRegisterEvent event) {}

    default void onSessionActive(AuthSessionActiveEvent event) {}

    default void onPremiumLogin(AuthPremiumLoginEvent event) {}

    default void onChangePassword(ChangePasswordEvent event) {}

    default void onLogout(LogoutEvent event) {}
}
