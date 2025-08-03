package com.beacmc.beacmcauth.bungeecord.server.command.sender;

import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;

public class BungeeCommandSender implements CommandSender {

    private final net.md_5.bungee.api.CommandSender sender;
    private final MessageProvider messageProvider;

    public BungeeCommandSender(net.md_5.bungee.api.CommandSender sender) {
        this.sender = sender;
        this.messageProvider = BungeeBeacmcAuth.getInstance().getBeacmcAuth().getMessageProvider();
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(messageProvider.createMessage(message).toBaseComponent());
    }

    @Override
    public boolean hasPermission(String perm) {
        return sender.hasPermission(perm);
    }
}
