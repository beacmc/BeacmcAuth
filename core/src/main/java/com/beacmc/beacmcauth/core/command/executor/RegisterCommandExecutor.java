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
            player.sendMessage(config.getMessages().getAlreadyAuthed());
            return;
        }

        final CompletableFuture<ProtectedPlayer> future = authManager.getProtectedPlayer(player.getLowercaseName());
        final Integer minLength = config.getPasswordMinLength();
        final Integer maxLength = config.getPasswordMaxLength();

        future.thenAccept(protectedPlayer -> {
            if (protectedPlayer.isRegister()) {
                player.sendMessage(config.getMessages().getAlreadyRegister());
                return;
            }

            if (args.length < 2) {
                player.sendMessage(config.getMessages().getConfirmPassword());
                return;
            }

            if (!args[0].equals(args[1])) {
                player.sendMessage(config.getMessages().getPasswordsDontMatch());
                return;
            }

            if (args[0].length() < minLength) {
                player.sendMessage(config.getMessages().getLowCharacterPassword());
                return;
            }

            if (args[0].length() > maxLength) {
                player.sendMessage(config.getMessages().getHighCharacterPassword());
                return;
            }

            player.sendMessage(config.getMessages().getRegisterSuccess());
            player.sendTitle("&7", "&7", 0, 25, 0);
            authManager.getAuthPlayers().remove(protectedPlayer.getLowercaseName());
            authManager.register(protectedPlayer, args[0]);
            authManager.connectPlayer(player, config.findServer(config.getLobbyServers()));
            authManager.onAzLinkRegister(player.getName(), player.getUUID(), args[0], player.getInetAddress());
        });
    }
}
