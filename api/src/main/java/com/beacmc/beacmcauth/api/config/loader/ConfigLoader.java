package com.beacmc.beacmcauth.api.config.loader;

import de.exlll.configlib.YamlConfigurationProperties;

import java.io.File;

public interface ConfigLoader {

    <T> T load(File file, Class<T> clazz, T obj);

    YamlConfigurationProperties getDefaultConfigProperties();
}
