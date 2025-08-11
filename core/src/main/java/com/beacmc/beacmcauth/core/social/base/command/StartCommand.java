package com.beacmc.beacmcauth.core.social.base.command;

import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.command.SocialCommand;

public class StartCommand implements SocialCommand {

    @Override
    public void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String prefix, String[] args) {
        if (prefix.startsWith("/start") || prefix.startsWith("Начать")) {
            socialPlayer.sendPrivateMessage(social.getSocialConfig().getMessages().getStartMessage());
        }
    }
}
