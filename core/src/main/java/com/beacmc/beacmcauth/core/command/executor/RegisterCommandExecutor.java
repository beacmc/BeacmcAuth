package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;

import java.util.regex.Pattern;

public class RegisterCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;
    private final GameCooldown cooldown;

    public RegisterCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
        this.cooldown = GameCooldown.getInstance();
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

        if (cooldown.isCooldown(player.getLowercaseName())) {
            player.sendMessage(config.getMessages().getCooldown());
            return;
        }
        cooldown.createCooldown(player.getLowercaseName(), 1_000);

        final Pattern passwordPattern = config.getPasswordRegex();
        final boolean repeatPassword = config.isRegisterRepeatPassword();

        authManager.getProtectedPlayer(player.getUUID()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) return;

            if (protectedPlayer.isRegister()) {
                player.sendMessage(config.getMessages().getAlreadyRegister());
                return;
            }

            if (repeatPassword && args.length < 2) {
                player.sendMessage(config.getMessages().getConfirmPassword());
                return;
            }

            if (repeatPassword && !args[0].equals(args[1])) {
                player.sendMessage(config.getMessages().getPasswordsDontMatch());
                return;
            }

            if (args.length < 1 || !passwordPattern.matcher(args[0]).matches()) {
                player.sendMessage(config.getMessages().getInvalidPassword());
                return;
            }

            String password = args[0];

            player.sendMessage(config.getMessages().getRegisterSuccess()
                    .replace("%password%", password));

            player.sendTitle("&7", "&7", 0, 25, 0);
            plugin.getSongManager().stop(player.getUUID());
            authManager.getAuthPlayers().remove(protectedPlayer.getLowercaseName());
            authManager.register(protectedPlayer, password);
            authManager.connectPlayer(player, config.findServer(config.getLobbyServers()));
        });
    }
}
