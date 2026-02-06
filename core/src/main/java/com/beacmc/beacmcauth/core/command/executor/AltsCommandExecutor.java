package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AltsCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("beacmcauth.admin")) return;

        final Config config = plugin.getConfig();
        final ConfigMessages messages = config.getMessages();

        if (args.length < 1) {
            sender.sendMessage(messages.getAltsCommandUsage());
            return;
        }

        authManager.getProtectedPlayer(args[0].toLowerCase()).thenAccept(player -> {
            if (player == null) {
                sender.sendMessage(messages.getAccountNotFound());
                return;
            }
            authManager.getAltAccounts(player.getRegisterIp()).thenAccept(altAccounts -> {
                if (altAccounts == null || altAccounts.getNames().size() < 2) {
                    sender.sendMessage(messages.getAltsAccountsEmpty());
                    return;
                }

                String message = String.join(", ", altAccounts.getNames());
                sender.sendMessage(message);
            });
        });
    }
}
