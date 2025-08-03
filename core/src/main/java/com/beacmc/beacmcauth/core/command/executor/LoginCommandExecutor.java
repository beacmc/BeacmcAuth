package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.player.ServerPlayer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LoginCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public LoginCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
    }

    @Override
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

        future.thenAccept(protectedPlayer -> {
            if (!protectedPlayer.isRegister()) {
                player.sendMessage(config.getMessage("not-register"));
                return;
            }

            if (args.length < 1) {
                player.sendMessage(config.getMessage("enter-password"));
                return;
            }

            if (!protectedPlayer.checkPassword(args[0])) {
                int attempts = authManager.getAuthPlayers().get(protectedPlayer.getLowercaseName());
                authManager.getAuthPlayers().put(protectedPlayer.getLowercaseName(), attempts - 1);

                player.sendMessage(config.getMessage("wrong-password", Map.of("%attempts%", String.valueOf(attempts - 1))));
                if (authManager.getAuthPlayers().get(protectedPlayer.getLowercaseName()) <= 0) {
                    player.disconnect(config.getMessage("attempts-left"));
                }
                return;
            }

            player.sendMessage(config.getMessage("login-success"));
            authManager.getAuthPlayers().remove(protectedPlayer.getLowercaseName());

            if (config.isAzLinkIntegration()) {
                authManager.onAzLinkRegister(player.getName(), player.getUUID(), args[0], player.getInetAddress());
            }

            if (!plugin.getSocialManager().startPlayerConfirmations(protectedPlayer)) {
                authManager.performLogin(protectedPlayer);
                authManager.connectPlayer(player, config.findServer(config.getGameServers()));
            }
        });
    }
}
