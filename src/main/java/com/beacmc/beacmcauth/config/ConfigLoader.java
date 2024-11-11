package com.beacmc.beacmcauth.config;

import com.beacmc.beacmcauth.database.DatabaseType;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Enum.valueOf;

public class ConfigLoader {

    public ConfigLoader() {
    }

    public void loadConfig(final File file, final Object object) {
        try {

            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            Class<?> clazz = object.getClass();

            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(ConfigValue.class)) {
                    continue;
                }

                ConfigValue annotation = field.getAnnotation(ConfigValue.class);
                String key = annotation.key();

                field.setAccessible(true);
                Object value = config.get(key);

                try {
                    if (value == null) {
                        value = getDefaultValue(field.getType());
                        if (value == null) continue;
                    }

                    if (field.getType().isEnum()) {
                        Class<Enum> enumType = (Class<Enum>) field.getType();
                        Enum<?> enumValue = Enum.valueOf(enumType, value.toString().toUpperCase());
                        field.set(object, enumValue);
                    } else if (field.getType() == List.class) {
                        field.set(object, value instanceof List ? value : Collections.emptyList());
                    } else if (field.getType() == Configuration.class) {
                        field.set(object, config.getSection(key));
                    } else if (field.getType() == Pattern.class && value instanceof String) {
                        field.set(object, Pattern.compile((String) value));
                    } else if (field.getType() == boolean.class) {
                        field.set(object, value);
                    } else if (field.getType().isInstance(value) || field.getType() == String.class) {
                        field.set(object, value);
                    }

                } catch (IllegalAccessException | IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == int.class) {
            return 0;
        } else if (type == boolean.class) {
            return false;
        } else if (type == long.class) {
            return 0L;
        } else if (type == List.class) {
            return Collections.emptyList();
        } else if (type.isEnum()) {
            return type.getEnumConstants()[0];
        } else if (type == Pattern.class) {
            return Pattern.compile("");
        }
        return null;
    }
}
