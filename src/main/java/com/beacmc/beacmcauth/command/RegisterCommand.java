package com.beacmc.beacmcauth.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.api.event.AccountLoginEvent;
import com.beacmc.beacmcauth.api.event.AccountRegisterEvent;
import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.j256.ormlite.stmt.query.In;
import kotlin.io.path.PathWalkOption;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RegisterCommand extends Command {

    private final AuthManager authManager;
    private final ProtectedPlayerDao dao;

    public RegisterCommand() {
        super("register", null, "reg");
        authManager = BeacmcAuth.getAuthManager();
        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
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
            player.sendMessage(config.getMessage("already-authed", Map.of()));
            return;
        }

        final CompletableFuture<ProtectedPlayer> future = ProtectedPlayer.get(player.getDisplayName().toLowerCase());
        final Integer minLength = config.getPasswordMinLength();
        final Integer maxLength = config.getPasswordMaxLength();

        future.thenAccept(protectedPlayer -> {
            if (protectedPlayer.isRegister()) {
                player.sendMessage(config.getMessage("already-register", Map.of()));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(config.getMessage("confirm-password", Map.of()));
                return;
            }

            if (!args[0].equals(args[1])) {
                player.sendMessage(config.getMessage("passwords-dont-match", Map.of()));
                return;
            }

            if (args[0].length() < minLength) {
                player.sendMessage(config.getMessage("low-character-password", Map.of()));
                return;
            }

            if (args[0].length() > maxLength) {
                player.sendMessage(config.getMessage("high-character-password", Map.of()));
                return;
            }

            AccountRegisterEvent event = new AccountRegisterEvent(protectedPlayer);
            ProxyServer.getInstance().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            player.sendMessage(config.getMessage("register-success", Map.of()));
            authManager.getAuthPlayers().remove(protectedPlayer.getLowercaseName());
            protectedPlayer.register(args[0]);
            authManager.connectPlayerToServer(player, config.findServerInfo(config.getGameServers()));
        });
    }
}
