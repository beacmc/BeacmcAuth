package com.beacmc.beacmcauth.telegram;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.TelegramConfig;
import com.beacmc.beacmcauth.telegram.link.TelegramLinkProvider;
import com.beacmc.beacmcauth.telegram.listener.BaseUpdateListener;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TelegramProvider {

    private TelegramBot bot;
    private final Logger logger;
    private final BeacmcAuth plugin;
    private final TelegramLinkProvider linkProvider;
    private final Map<String, ProtectedPlayer> confirmationUsers;

    public TelegramProvider() {
        final TelegramConfig telegramConfig = BeacmcAuth.getTelegramConfig();

        confirmationUsers = new HashMap<>();
        plugin = BeacmcAuth.getInstance();
        logger = plugin.getLogger();
        linkProvider = new TelegramLinkProvider(this);
        if (telegramConfig.isEnabled()) init();
    }

    public void init() {
        final TelegramConfig telegramConfig = BeacmcAuth.getTelegramConfig();

        logger.info("[Telegram] The bot turning on...");
        bot = new TelegramBot(telegramConfig.getToken());

        bot.setUpdatesListener(new BaseUpdateListener(this));
        logger.info("[Telegram] The bot has been successfully switched on");
    }

    public void sendConfirmationMessage(ProtectedPlayer protectedPlayer) {
        final TelegramConfig telegramConfig = BeacmcAuth.getTelegramConfig();
        final ProxiedPlayer player = protectedPlayer.getPlayer();
        final long telegram = protectedPlayer.getTelegram();

        if (telegram == 0 || player == null)
            return;

        String ip = player.getAddress().getHostName();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton(telegramConfig.getMessage("confirmation-button-accept-text")).callbackData("confirm-accept-" + protectedPlayer.getLowercaseName()),
                new InlineKeyboardButton(telegramConfig.getMessage("confirmation-button-decline-text")).callbackData("confirm-decline-" + protectedPlayer.getLowercaseName())
        );
        SendMessage sendMessage = new SendMessage(telegram, telegramConfig.getMessage("confirmation-message", Map.of("%name%", player.getDisplayName(), "%ip%", ip)));
        sendMessage.replyMarkup(markup);

        SendResponse response = bot.execute(sendMessage);
        if (!response.isOk()) {
            logger.severe("[Telegram] Error on send message: " + response.errorCode() + " - " + response.description());
        }
    }

    public Map<String, ProtectedPlayer> getConfirmationUsers() {
        return confirmationUsers;
    }

    public boolean isEnabled() {
        return bot != null;
    }

    public TelegramBot getBot() {
        return bot;
    }

    public TelegramLinkProvider getLinkProvider() {
        return linkProvider;
    }


}
