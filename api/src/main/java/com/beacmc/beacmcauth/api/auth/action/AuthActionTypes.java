package com.beacmc.beacmcauth.api.auth.action;

public enum AuthActionTypes implements AuthActionType {

    REGISTER,
    LOGIN,
    OPEN_AUTH_DIALOG,
    PREMIUM_LOGIN,
    CHANGE_PASSWORD;

    @Override
    public String getIdentifier() {
        return name().replace('_', '-')
                .toLowerCase();
    }

    static {
        AuthActionRegistry.INSTANCE.registerActions(values());
    }
}
