package com.beacmc.beacmcauth.api.command;

public interface CommandSender {

    void sendMessage(String message);

    boolean hasPermission(String perm);
}
