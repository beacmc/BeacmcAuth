package com.beacmc.beacmcauth.api;

import java.util.Map;

public interface PlaceholderSupport {
    default String parsePlaceholders(String text, Map<String, ?> placeholders) {
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
