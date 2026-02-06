package com.beacmc.beacmcauth.core.util;

import java.util.Map;

public class PlaceholderUtil {

    public static String parse(String plainText, Map<String, ?> placeholders) {
        if (plainText == null || placeholders == null || placeholders.isEmpty()) return plainText;

        String replacedText = plainText;
        for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
            replacedText = replacedText.replace(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return replacedText;
    }
}
