package com.beacmc.beacmcauth.core.social.base.command;

import com.beacmc.beacmcauth.api.social.command.SocialCommand;
import com.beacmc.beacmcauth.api.social.command.SocialCommandRegistry;

import java.util.ArrayList;
import java.util.List;

public class BaseSocialCommandRegistry implements SocialCommandRegistry {

    private final List<SocialCommand> commands = new ArrayList<>();

    @Override
    public List<SocialCommand> getCommands() {
        return commands;
    }
}
