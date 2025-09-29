package com.beacmc.beacmcauth.api.social.command;

import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;

import java.util.List;

public interface SocialCommandRegistry {

    List<SocialCommand> getCommands();

    default void executeCommands(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String prefix, String[] args) {
        if (socialPlayer != null && social != null && prefix != null && args != null) {
            getCommands().forEach(command -> command.execute(socialPlayer, social, prefix, args));
        }
    }
}
