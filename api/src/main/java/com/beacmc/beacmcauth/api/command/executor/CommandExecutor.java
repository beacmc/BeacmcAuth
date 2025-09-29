package com.beacmc.beacmcauth.api.command.executor;

import com.beacmc.beacmcauth.api.command.CommandSender;

public interface CommandExecutor {

    void execute(CommandSender sender, String[] args);
}
