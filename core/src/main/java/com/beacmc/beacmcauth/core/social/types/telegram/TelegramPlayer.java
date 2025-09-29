package com.beacmc.beacmcauth.core.social.types.telegram;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class TelegramPlayer implements SocialPlayer<User, Long> {

    private final User user;
    private final TelegramSocial social;

    public TelegramPlayer(TelegramSocial social, User user) {
        this.user = user;
        this.social = social;
    }

    @Override
    public void sendPrivateMessage(String message, @Nullable Object keyboard) {
        SendMessage sendMessage = new SendMessage(getID(), message);
        if (keyboard instanceof InlineKeyboardMarkup markup) {
            sendMessage.replyMarkup(markup);
        }
        social.execute(sendMessage);
    }

    @Override
    public boolean checkAccountLink(ProtectedPlayer player) {
        return player != null && getID() == player.getTelegram();
    }

    @Override
    public Long getID() {
        return user.id();
    }

    @Override
    public User getOriginalSocialPlayer() {
        return user;
    }
}
