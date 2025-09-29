package com.beacmc.beacmcauth.bungeecord.server.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.bungeecord.player.BungeeServerPlayer;
import com.beacmc.beacmcauth.bungeecord.server.command.sender.BungeeCommandSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AuthCommand extends Command {

    private final CommandManager commandManager;

    public AuthCommand(BeacmcAuth plugin) {
        super("beacmcauth", "beacmcauth.admin", "bauth", "auth");
        commandManager = plugin.getCommandManager();
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        com.beacmc.beacmcauth.api.command.CommandSender sender = (commandSender instanceof ProxiedPlayer)
                ? new BungeeServerPlayer((ProxiedPlayer) commandSender)
                : new BungeeCommandSender(commandSender);

        commandManager.getCommandByName("auth")
                .execute(sender, strings);
    }
}
