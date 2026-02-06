package com.beacmc.beacmcauth.core.social.types.telegram.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.social.types.telegram.TelegramPlayer;
import com.beacmc.beacmcauth.core.social.types.telegram.TelegramSocial;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AllArgsConstructor
public class TelegramUpdatesListener implements UpdatesListener {

    private final BeacmcAuth plugin;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Override
    public int process(List<Update> updates) {
        final SocialManager manager = plugin.getSocialManager();
        final TelegramSocial telegram = (TelegramSocial) manager.getSocialByType(SocialType.TELEGRAM);

        if (telegram == null || updates.isEmpty())
            return CONFIRMED_UPDATES_NONE;

        Update update = updates.get(0);

        if (update.message() != null && update.message().text() != null) {
            String text = update.message().text();
            String[] args = text.split("\\s+");
            if (args.length > 0) {
                String replacedText = text.replace(args[0], "").trim();
                manager.getSocialCommandRegistry().executeCommands(
                        new TelegramPlayer(telegram, update.message().from()),
                        telegram,
                        args[0],
                        replacedText.split("\\s+")
                );
            }
        }

        if (update.callbackQuery() != null && update.callbackQuery().data() != null) {
            String callbackId = update.callbackQuery().id();
            telegram.getTelegramBot().execute(new AnswerCallbackQuery(callbackId));

            CompletableFuture.supplyAsync(() -> {
                Button button = Button.builder()
                        .type(ButtonType.SECONDARY)
                        .label("")
                        .callbackData(update.callbackQuery().data())
                        .build();

                manager.getButtonClickRegistry().executeListeners(new TelegramPlayer(telegram, update.callbackQuery().from()), telegram, button);
                return null;
            }, executorService);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
