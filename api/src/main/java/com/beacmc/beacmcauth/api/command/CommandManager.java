package com.beacmc.beacmcauth.api.command;

import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;

import java.util.Map;

public interface CommandManager {

    default CommandExecutor getCommandByName(String name) {
        return getCommandExecutors().get(name);
    }


    default void register(String name, CommandExecutor commandExecutor) {
        if (!getCommandExecutors().containsKey(name)) {
            getCommandExecutors().put(name, commandExecutor);
        }
    }

    default void unregister(String name) {
        getCommandExecutors().remove(name);
    }

    Map<String, CommandExecutor> getCommandExecutors();
}
