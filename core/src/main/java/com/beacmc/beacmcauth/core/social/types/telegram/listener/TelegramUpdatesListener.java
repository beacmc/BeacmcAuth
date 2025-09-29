package com.beacmc.beacmcauth.core.social.types.telegram.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.social.types.telegram.TelegramPlayer;
import com.beacmc.beacmcauth.core.social.types.telegram.TelegramSocial;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TelegramUpdatesListener implements UpdatesListener {

    private final BeacmcAuth plugin;

    @Override
    public int process(List<Update> updates) {
        final SocialManager manager = plugin.getSocialManager();
        final Social<?, ?> telegram = manager.getSocialByType(SocialType.TELEGRAM);

        if (telegram == null)
            return CONFIRMED_UPDATES_NONE;

        updates.forEach(update -> {
            if (update.message() != null && update.message().text() != null) {
                String text = update.message().text();
                String[] args = text.split("\\s+");
                if (args.length > 0) {
                    String replacedText = text.replace(args[0], "").trim();
                    manager.getSocialCommandRegistry().executeCommands(
                            new TelegramPlayer((TelegramSocial) telegram, update.message().from()),
                            telegram,
                            args[0],
                            replacedText.split("\\s+")
                    );
                }
            }

            if (update.callbackQuery() != null && update.callbackQuery().data() != null) {
                Button button = Button.builder()
                        .type(ButtonType.SECONDARY)
                        .label("")
                        .callbackData(update.callbackQuery().data())
                        .build();

                manager.getButtonClickRegistry().executeListeners(new TelegramPlayer((TelegramSocial) telegram, update.callbackQuery().from()), telegram, button);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
