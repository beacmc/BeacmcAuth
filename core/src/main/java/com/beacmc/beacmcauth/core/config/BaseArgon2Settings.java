package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.config.Argon2Settings;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseArgon2Settings implements Argon2Settings {

    private int iterations = 3;
    private int memoryKb = 65536;
    private int parallelism = 1;
}
