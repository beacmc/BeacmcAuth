package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.player.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class RegisterCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public RegisterCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) {
            sender.sendMessage("Only player");
            return;
        }

        final Config config = plugin.getConfig();

        if (!authManager.getAuthPlayers().containsKey(player.getName().toLowerCase())) {
            player.sendMessage(config.getMessage("already-authed"));
            return;
        }

        final CompletableFuture<ProtectedPlayer> future = authManager.getProtectedPlayer(player.getLowercaseName());
        final Integer minLength = config.getPasswordMinLength();
        final Integer maxLength = config.getPasswordMaxLength();

        future.thenAccept(protectedPlayer -> {
            if (protectedPlayer.isRegister()) {
                player.sendMessage(config.getMessage("already-register"));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(config.getMessage("confirm-password"));
                return;
            }

            if (!args[0].equals(args[1])) {
                player.sendMessage(config.getMessage("passwords-dont-match"));
                return;
            }

            if (args[0].length() < minLength) {
                player.sendMessage(config.getMessage("low-character-password"));
                return;
            }

            if (args[0].length() > maxLength) {
                player.sendMessage(config.getMessage("high-character-password"));
                return;
            }

            player.sendMessage(config.getMessage("register-success"));
            authManager.getAuthPlayers().remove(protectedPlayer.getLowercaseName());
            authManager.register(protectedPlayer, args[0]);
            authManager.connectPlayer(player, config.findServer(config.getGameServers()));
            authManager.onAzLinkRegister(player.getName(), player.getUUID(), args[0], player.getInetAddress());
        });
    }
}
