package com.beacmc.beacmcauth.core.config.serializer;

import de.exlll.configlib.Serializer;

import java.util.regex.Pattern;

public class PatternSerializer implements Serializer<Pattern, String> {

    @Override
    public String serialize(Pattern element) {
        return element.pattern();
    }

    @Override
    public Pattern deserialize(String element) {
        return Pattern.compile(element);
    }
}
