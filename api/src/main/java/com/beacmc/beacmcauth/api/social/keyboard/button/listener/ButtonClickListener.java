package com.beacmc.beacmcauth.api.social.keyboard.button.listener;

import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;

public interface ButtonClickListener {

    void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, Button button);
}
