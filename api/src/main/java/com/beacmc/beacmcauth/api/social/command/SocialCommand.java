package com.beacmc.beacmcauth.api.social.command;

import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;

public interface SocialCommand {

    void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String prefix, String[] args);
}
