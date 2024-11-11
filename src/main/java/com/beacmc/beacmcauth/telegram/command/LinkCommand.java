package com.beacmc.beacmcauth.telegram.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.config.impl.TelegramConfig;
import com.beacmc.beacmcauth.telegram.TelegramProvider;
import com.beacmc.beacmcauth.telegram.link.TelegramLinkProvider;
import com.beacmc.beacmcauth.util.CodeGenerator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

public class LinkCommand {

    public static void process(TelegramProvider telegramProvider, Update update) {
        final TelegramConfig telegramConfig = BeacmcAuth.getTelegramConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final TelegramLinkProvider linkProvider = telegramProvider.getLinkProvider();

        if (update.message() == null) return;

        String message = update.message().text();
        Long chatId = update.message().chat().id();
        if (message == null) return;

        String[] args = message.split("\\s+");

        if (!message.startsWith(telegramConfig.getLinkCommand()))
            return;

        if (args.length < 2) {
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-usage"));
            bot.execute(sendMessage);
            return;
        }

        String name = args[1].toLowerCase();

        ProtectedPlayer.get(name).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-player-not-found"));
                bot.execute(sendMessage);
                return;
            }

            ProxiedPlayer player = protectedPlayer.getPlayer();

            if (player == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-player-offline"));
                bot.execute(sendMessage);
                return;
            }

            if (protectedPlayer.getTelegram() != 0) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-already-linked"));
                bot.execute(sendMessage);
                return;
            }

            if (linkProvider.getProtectedPlayersById(update.message().from().id()).size() < telegramConfig.getMaxLink()) {
                String code = CodeGenerator.generate(telegramConfig.getCodeChars(), telegramConfig.getCodeLength());
                linkProvider.getLinkCodes().put(protectedPlayer.setLinkCode(code), update.message().from().id());
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-message", Map.of("%name%", player.getDisplayName(), "%code%", code)));
                bot.execute(sendMessage);
                return;
            }
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-limit"));
            bot.execute(sendMessage);
        });
    }
}
