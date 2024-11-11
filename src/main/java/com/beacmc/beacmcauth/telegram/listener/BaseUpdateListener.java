package com.beacmc.beacmcauth.telegram.listener;

import com.beacmc.beacmcauth.telegram.TelegramProvider;
import com.beacmc.beacmcauth.telegram.command.AccountsCommand;
import com.beacmc.beacmcauth.telegram.command.LinkCommand;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import java.util.List;

public class BaseUpdateListener implements UpdatesListener {

    private final TelegramProvider telegramProvider;

    public BaseUpdateListener(TelegramProvider telegramProvider) {
        this.telegramProvider = telegramProvider;
    }

    @Override
    public int process(List<Update> list) {
        for (Update update : list) {
            ConfirmationListener.process(telegramProvider, update);
            LinkCommand.process(telegramProvider, update);
            AccountsCommand.process(telegramProvider, update);
            AccountsCommand.processButtons(telegramProvider, update);
            AccountListener.process(telegramProvider, update);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
