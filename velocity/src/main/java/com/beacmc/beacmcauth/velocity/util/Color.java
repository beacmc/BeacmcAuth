package com.beacmc.beacmcauth.velocity.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Color {

    public static Component of(String content) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(content);
    }
}
