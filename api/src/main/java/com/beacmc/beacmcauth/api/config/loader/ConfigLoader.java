package com.beacmc.beacmcauth.api.config.loader;

import java.io.File;

public interface ConfigLoader {

    void loadConfig(File file, Object object);

    Object get(String path, Object def);

    default Object get(String path) {
        return get(path, null);
    }

    default String getString(String path, String def) {
        return String.valueOf(get(path, def));
    }

    default boolean getBoolean(String path, boolean def) {
        return (Boolean) get(path, def);
    }
}
