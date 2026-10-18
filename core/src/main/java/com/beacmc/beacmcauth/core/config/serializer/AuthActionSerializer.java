package com.beacmc.beacmcauth.core.config.serializer;

import com.beacmc.beacmcauth.api.auth.action.AuthActionRegistry;
import com.beacmc.beacmcauth.api.auth.action.AuthActionType;
import de.exlll.configlib.Serializer;

public class AuthActionSerializer implements Serializer<AuthActionType, String> {

    @Override
    public String serialize(AuthActionType element) {
        return element != null ? element.getIdentifier() : null;
    }

    @Override
    public AuthActionType deserialize(String element) {
        return AuthActionRegistry.INSTANCE.getByIdentifier(element);
    }
}
