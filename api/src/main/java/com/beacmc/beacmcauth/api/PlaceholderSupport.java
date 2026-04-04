package com.beacmc.beacmcauth.api;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface PlaceholderSupport {
    default String parsePlaceholders(@Nullable String text, @Nullable Map<String, ?> placeholders) {
        if (text == null || placeholders == null)
            return text;

        String replacedText = text;
        for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
            replacedText = replacedText.replace(
                    entry.getKey(),
                    String.valueOf(entry.getValue())
            );
        }
        return replacedText;
    }
}
