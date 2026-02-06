package com.beacmc.beacmcauth.bungeecord.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static BaseComponent of(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hexCode = matcher.group();
            StringBuilder formatted = new StringBuilder("§x");
            for (char c : hexCode.substring(1).toCharArray()) {
                formatted.append("§").append(c);
            }
            String bungeeHexFormat = formatted.toString();
            matcher.appendReplacement(buffer, bungeeHexFormat);
        }
        matcher.appendTail(buffer);

        return TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', buffer.toString())
                .replace("&", ""));
    }
}
