package com.beacmc.beacmcauth.api.social.keyboard.button.listener;

import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;

import java.util.List;

public interface ButtonClickRegistry {

    List<ButtonClickListener> getListeners();

    default void executeListeners(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, Button button) {
        if (socialPlayer != null && social != null && button != null) {
            getListeners().forEach(listener -> listener.execute(socialPlayer, social, button));
        }
    }
}
