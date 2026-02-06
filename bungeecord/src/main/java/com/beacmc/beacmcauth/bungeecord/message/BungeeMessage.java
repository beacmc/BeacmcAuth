package com.beacmc.beacmcauth.bungeecord.message;

import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.bungeecord.util.Color;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BungeeMessage implements Message {

    private final String content;
    private static final Pattern HOVER_PATTERN =
            Pattern.compile("\\{([^|]+)\\|hover:([^}]+)}");

    public BungeeMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContentRaw() {
        return content;
    }

    @Override
    public BaseComponent toBaseComponent() {
        return parseHover(getContentRaw());
    }

    @Override
    public Component toComponent() {
        return null;
    }

    public static BaseComponent parseHover(String message) {
        ComponentBuilder builder = new ComponentBuilder();

        int lastEnd = 0;
        Matcher matcher = HOVER_PATTERN.matcher(message);

        while (matcher.find()) {

            if (matcher.start() > lastEnd) {
                builder.append(Color.of(message.substring(lastEnd, matcher.start())));
            }

            BaseComponent text = Color.of(matcher.group(1));
            BaseComponent hover = Color.of(matcher.group(2));

            TextComponent comp = new TextComponent();
            comp.addExtra(text);
            comp.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new Text(hover)
            ));

            builder.append(comp);

            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            builder.append(Color.of(message.substring(lastEnd)));
        }

        return builder.build();
    }
}
