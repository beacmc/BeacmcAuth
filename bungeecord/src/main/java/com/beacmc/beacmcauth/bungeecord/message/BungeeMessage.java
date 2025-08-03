package com.beacmc.beacmcauth.bungeecord.message;

import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.bungeecord.util.Color;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeMessage implements Message {

    private final String content;

    public BungeeMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContentRaw() {
        return content;
    }

    @Override
    public BaseComponent[] toBaseComponent() {
        return Color.of(content);
    }

    @Override
    public Component toComponent() {
        return null;
    }
}
