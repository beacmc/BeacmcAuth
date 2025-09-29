package com.beacmc.beacmcauth.velocity.message;

import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.api.message.MessageProvider;

public class VelocityMessageProvider implements MessageProvider {

    @Override
    public Message createMessage(String content) {
        return new VelocityMessage(content);
    }
}
