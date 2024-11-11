package com.beacmc.beacmcauth.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.discord.link.DiscordLinkProvider;
import com.beacmc.beacmcauth.telegram.TelegramProvider;
import com.beacmc.beacmcauth.telegram.link.TelegramLinkProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LinkCommand extends Command {

    private final DiscordProvider discordProvider;
    private final DiscordLinkProvider discordLinkProvider;
    private final TelegramLinkProvider telegramLinkProvider;
    private final TelegramProvider telegramProvider;
    private final ProtectedPlayerDao dao;

    public LinkCommand() {
        super(BeacmcAuth.getConfig().getLinkCommand());

        discordProvider = BeacmcAuth.getDiscordProvider();
        telegramProvider = BeacmcAuth.getTelegramProvider();

        discordLinkProvider = discordProvider != null ? discordProvider.getLinkProvider() : null;
        telegramLinkProvider = telegramProvider != null ? telegramProvider.getLinkProvider() : null;

        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("Only player");
            return;
        }

        final BaseConfig config = BeacmcAuth.getConfig();
        final ProxiedPlayer player = (ProxiedPlayer) sender;
        ProtectedPlayer protectedPlayer = null;

        if (discordLinkProvider != null) {
            protectedPlayer = discordLinkProvider.getPlayerByName(player.getName().toLowerCase());
        }

        if (protectedPlayer == null && telegramLinkProvider != null) {
            protectedPlayer = telegramLinkProvider.getPlayerByName(player.getName().toLowerCase());
        }

        if (protectedPlayer == null) {
            player.sendMessage(config.getMessage("link-code-absent", Map.of()));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(config.getMessage("link-command-usage", Map.of()));
            return;
        }

        String code = protectedPlayer.getLinkCode();
        long discord = discordLinkProvider != null ? discordLinkProvider.getLinkCodes().getOrDefault(protectedPlayer, 0L) : 0L;
        long telegram = telegramLinkProvider != null ? telegramLinkProvider.getLinkCodes().getOrDefault(protectedPlayer, 0L) : 0L;

        if (!code.equals(args[0])) {
            player.sendMessage(config.getMessage("incorrect-code", Map.of()));
            return;
        }

        final ProtectedPlayer finalProtectPlayer = protectedPlayer;
        CompletableFuture.supplyAsync(() -> {
            try {
                player.sendMessage(config.getMessage("link-success"));

                if (telegramLinkProvider != null) {
                    telegramLinkProvider.getLinkCodes().remove(finalProtectPlayer);
                }
                if (discordLinkProvider != null) {
                    discordLinkProvider.getLinkCodes().remove(finalProtectPlayer);
                }

                if (discord != 0L) {
                    dao.createOrUpdate(finalProtectPlayer.setDiscord(discord));
                }
                if (telegram != 0L) {
                    dao.createOrUpdate(finalProtectPlayer.setTelegram(telegram));
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
