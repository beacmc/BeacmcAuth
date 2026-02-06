package com.beacmc.beacmcauth.velocity.server.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.velocity.player.VelocityServerPlayer;
import com.beacmc.beacmcauth.velocity.server.command.sender.VelocityCommandSender;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class EmailCommand implements SimpleCommand {

    private final CommandManager commandManager;

    public EmailCommand(BeacmcAuth plugin) {
        commandManager = plugin.getCommandManager();
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSender sender = (invocation.source() instanceof Player)
                ? new VelocityServerPlayer((Player) invocation.source())
                : new VelocityCommandSender(invocation.source());

        commandManager.getCommandByName("email")
                .execute(sender, invocation.arguments());
    }
}
