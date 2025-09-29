package com.beacmc.beacmcauth.core.command;

import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;

import java.util.HashMap;
import java.util.Map;

public class BaseCommandManager implements CommandManager {

    private final Map<String, CommandExecutor> commandExecutors;

    public BaseCommandManager() {
        commandExecutors = new HashMap<>();
    }

    @Override
    public Map<String, CommandExecutor> getCommandExecutors() {
        return commandExecutors;
    }
}
