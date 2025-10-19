package com.beacmc.beacmcauth.core.config.loader;

import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.core.config.serializer.PatternSerializer;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class BaseConfigLoader implements ConfigLoader {

    @Override
    public <T> T load(File file, Class<T> clazz, T obj) {
        Path filePath = file.toPath();
        if (!file.exists()) {
            YamlConfigurations.save(filePath, clazz, obj, getDefaultConfigProperties());
        }

        return YamlConfigurations.load(filePath, clazz, getDefaultConfigProperties());
    }

    @Override
    public YamlConfigurationProperties getDefaultConfigProperties() {
        return YamlConfigurationProperties.newBuilder()
                .addSerializer(Pattern.class, new PatternSerializer())
                .build();
    }
}
