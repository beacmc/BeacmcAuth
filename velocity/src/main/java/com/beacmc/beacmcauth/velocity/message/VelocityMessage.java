package com.beacmc.beacmcauth.velocity.message;

import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.velocity.util.Color;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public class VelocityMessage implements Message {

    private final String content;

    public VelocityMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContentRaw() {
        return content;
    }

    @Override
    public BaseComponent[] toBaseComponent() {
        return null;
    }

    @Override
    public Component toComponent() {
        return Color.of(getContentRaw());
    }
}
