package com.beacmc.beacmcauth.bungeecord.message;

import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.api.message.MessageProvider;

public class BungeeMessageProvider implements MessageProvider {

    @Override
    public Message createMessage(String content) {
        return new BungeeMessage(content);
    }
}
