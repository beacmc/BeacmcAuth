package com.beacmc.beacmcauth.api.auth.action;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuthActionRegistry {

    public static final AuthActionRegistry INSTANCE = new AuthActionRegistry();

    private final List<AuthActionType> authActionTypes = new CopyOnWriteArrayList<>();

    public void registerAction(AuthActionType authActionType) {
        authActionTypes.add(authActionType);
    }

    public void registerActions(AuthActionType... authActionType) {
        for (AuthActionType actionType : authActionType) {
            registerAction(actionType);
        }
    }

    public void unregisterAction(AuthActionType authActionType) {
        authActionTypes.remove(authActionType);
    }

    public AuthActionType getByIdentifier(String identifier) {
        return authActionTypes.stream()
                .filter(authActionType -> authActionType.getIdentifier().equals(identifier))
                .findFirst()
                .orElse(null);
    }
}
