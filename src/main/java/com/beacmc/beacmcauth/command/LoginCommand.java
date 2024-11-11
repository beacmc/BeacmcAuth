package com.beacmc.beacmcauth.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.api.event.AccountLoginEvent;
import com.beacmc.beacmcauth.api.event.AccountSessionActiveEvent;
import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.telegram.TelegramProvider;
import com.beacmc.beacmcauth.util.runnable.impl.DiscordRunnable;
import com.beacmc.beacmcauth.util.runnable.impl.TelegramRunnable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LoginCommand extends Command {

    private final AuthManager authManager;
    private final ProtectedPlayerDao dao;
    private final DiscordProvider discord;

    private final TelegramProvider telegram;

    public LoginCommand() {
        super("login", null, "log", "l");
        authManager = BeacmcAuth.getAuthManager();
        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
        discord = BeacmcAuth.getDiscordProvider();
        telegram = BeacmcAuth.getTelegramProvider();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText("Only player"));
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) sender;
        final BaseConfig config = BeacmcAuth.getConfig();

        if (!authManager.getAuthPlayers().containsKey(player.getDisplayName().toLowerCase())) {
            player.sendMessage(config.getMessage("already-authed"));
            return;
        }

        final CompletableFuture<ProtectedPlayer> future = ProtectedPlayer.get(player.getDisplayName().toLowerCase());

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

            AccountLoginEvent event = new AccountLoginEvent(protectedPlayer);
            ProxyServer.getInstance().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            player.sendMessage(config.getMessage("login-success"));
            authManager.getAuthPlayers().remove(protectedPlayer.getLowercaseName());

            if (protectedPlayer.getDiscord() != 0 && discord != null && protectedPlayer.isDiscordTwoFaEnabled()) {
                discord.sendConfirmationMessage(protectedPlayer);
                discord.getConfirmationUsers().put(protectedPlayer.getLowercaseName(), protectedPlayer);
                new DiscordRunnable(protectedPlayer);
                return;
            }

            if (protectedPlayer.getTelegram() != 0 && telegram != null && protectedPlayer.isTelegramTwoFaEnabled()) {
                telegram.sendConfirmationMessage(protectedPlayer);
                telegram.getConfirmationUsers().put(protectedPlayer.getLowercaseName(), protectedPlayer);
                new TelegramRunnable(protectedPlayer);
                return;
            }

            protectedPlayer.performLogin();
            authManager.connectPlayerToServer(player, config.findServerInfo(config.getGameServers()));
        });
    }
}
