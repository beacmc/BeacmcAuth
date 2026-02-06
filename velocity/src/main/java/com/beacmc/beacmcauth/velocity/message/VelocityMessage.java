package com.beacmc.beacmcauth.velocity.message;

import com.beacmc.beacmcauth.api.message.Message;
import com.beacmc.beacmcauth.velocity.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VelocityMessage implements Message {

    private final String content;
    private static final Pattern HOVER_PATTERN =
            Pattern.compile("\\{([^|]+)\\|hover:([^}]+)}");


    public VelocityMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContentRaw() {
        return content;
    }

    @Override
    public BaseComponent toBaseComponent() {
        return null;
    }

    @Override
    public Component toComponent() {
        return parseHover(getContentRaw());
    }

    public static Component parseHover(String message) {
        Component result = Component.empty();

        int lastEnd = 0;
        Matcher matcher = HOVER_PATTERN.matcher(message);

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String part = message.substring(lastEnd, matcher.start());
                result = result.append(Color.of(part));
            }

            String text = matcher.group(1);
            String hover = matcher.group(2);

            Component comp = Color.of(text)
                    .hoverEvent(HoverEvent.showText(Color.of(hover)));

            result = result.append(comp);
            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            result = result.append(Color.of(message.substring(lastEnd)));
        }

        return result;
    }
}
