package com.beacmc.beacmcauth.telegram.listener;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.api.event.AccountDiscordConfirmEvent;
import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.config.impl.TelegramConfig;
import com.beacmc.beacmcauth.telegram.TelegramProvider;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ConfirmationListener {


    public static void process(TelegramProvider telegramProvider, Update update) {
        final TelegramConfig telegramConfig = BeacmcAuth.getTelegramConfig();
        final BaseConfig config = BeacmcAuth.getConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final AuthManager authManager = BeacmcAuth.getAuthManager();

        if (update.callbackQuery() == null) return;

        String callbackData = update.callbackQuery().data();
        Long chatId = update.callbackQuery().from().id();

        if (callbackData != null && callbackData.startsWith("confirm-accept-")) {
            String[] split = callbackData.split("confirm-accept-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!telegramProvider.getConfirmationUsers().containsKey(name)) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("no-confirmation"));
                bot.execute(sendMessage);
                return;
            }
            ProtectedPlayer protectedPlayer = telegramProvider.getConfirmationUsers().get(name);

            AccountDiscordConfirmEvent confirmEvent = new AccountDiscordConfirmEvent(protectedPlayer);
            ProxyServer.getInstance().getPluginManager().callEvent(confirmEvent);
            if (confirmEvent.isCancelled()) return;

            telegramProvider.getConfirmationUsers().remove(name);
            protectedPlayer.performLogin();
            ProxiedPlayer proxiedPlayer = protectedPlayer.getPlayer();
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("confirmation-success"));
            bot.execute(sendMessage);

            if (proxiedPlayer != null) {
                proxiedPlayer.sendMessage(config.getMessage("discord-confirmation-success"));
                authManager.connectPlayerToServer(proxiedPlayer, config.findServerInfo(config.getGameServers()));
            }
        }

        if (callbackData != null && callbackData.startsWith("confirm-decline-")) {
            String[] split = callbackData.split("confirm-decline-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!telegramProvider.getConfirmationUsers().containsKey(name)) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("no-confirmation"));
                bot.execute(sendMessage);
                return;
            }

            ProtectedPlayer player = telegramProvider.getConfirmationUsers().get(name);
            telegramProvider.getConfirmationUsers().remove(name);
            ProxiedPlayer proxiedPlayer = player.getPlayer();
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("confirmation-denied"));
            bot.execute(sendMessage);
            if (proxiedPlayer != null) {
                proxiedPlayer.disconnect(config.getMessage("discord-confirmation-denied"));
            }
        }
    }
}
