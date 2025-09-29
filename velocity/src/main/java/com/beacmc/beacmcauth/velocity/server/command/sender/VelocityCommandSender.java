package com.beacmc.beacmcauth.velocity.server.command.sender;

import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import com.velocitypowered.api.command.CommandSource;

public class VelocityCommandSender implements CommandSender {

    private final CommandSource commandSource;
    private final MessageProvider messageProvider;

    public VelocityCommandSender(CommandSource commandSource) {
        this.messageProvider = VelocityBeacmcAuth.getInstance().getBeacmcAuth().getMessageProvider();
        this.commandSource = commandSource;
    }

    @Override
    public boolean hasPermission(String perm) {
        return commandSource.hasPermission(perm);
    }

    @Override
    public void sendMessage(String message) {
        commandSource.sendMessage(messageProvider.createMessage(message).toComponent());
    }
}
