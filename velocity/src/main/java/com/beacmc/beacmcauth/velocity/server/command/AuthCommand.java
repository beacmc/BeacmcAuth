package com.beacmc.beacmcauth.velocity.server.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.velocity.player.VelocityServerPlayer;
import com.beacmc.beacmcauth.velocity.server.command.sender.VelocityCommandSender;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class AuthCommand implements SimpleCommand {

    private final CommandManager commandManager;

    public AuthCommand(BeacmcAuth plugin) {
        commandManager = plugin.getCommandManager();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSender sender = (invocation.source() instanceof Player)
                ? new VelocityServerPlayer((Player) invocation.source())
                : new VelocityCommandSender(invocation.source());

        commandManager.getCommandByName("auth")
                .execute(sender, invocation.arguments());
    }
}
