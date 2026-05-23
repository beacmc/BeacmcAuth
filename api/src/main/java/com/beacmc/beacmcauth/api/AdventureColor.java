package com.beacmc.beacmcauth.api;

import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class AdventureColor {

    public static Component of(String content) {
        if (content == null) return Component.empty();

        return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(content)
                .decoration(TextDecoration.ITALIC, false);
    }
}
