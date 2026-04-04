package com.beacmc.beacmcauth.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AdventureColor {

    public static Component of(String content) {
        if (content == null) return Component.empty();

        return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(content)
                .decoration(TextDecoration.ITALIC, false);
    }
}
