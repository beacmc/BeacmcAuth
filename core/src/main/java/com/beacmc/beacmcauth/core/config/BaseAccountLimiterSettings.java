package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.config.AccountLimiterSettings;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseAccountLimiterSettings implements AccountLimiterSettings {

    private boolean enabled = true;
    private int limit = 4;
}
