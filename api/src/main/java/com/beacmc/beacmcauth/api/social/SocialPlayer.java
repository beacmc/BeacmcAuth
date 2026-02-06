package com.beacmc.beacmcauth.api.social;

import com.beacmc.beacmcauth.api.model.ProtectedPlayer;

public interface SocialPlayer<T, ID> {

    void sendPrivateMessage(String message, Object keyboard);

    default void sendPrivateMessage(String message) {
        sendPrivateMessage(message, null);
    }

    ID getID();

    boolean checkAccountLink(ProtectedPlayer player);

    T getOriginalSocialPlayer();
}
