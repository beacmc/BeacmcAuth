package com.beacmc.beacmcauth.api.auth.premium;

import com.beacmc.beacmcauth.api.cache.CachedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Temporary or permanent premium player
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class PremiumPlayer implements CachedData<String> {

    private final String lowercaseName;
    private boolean template;
    private long templateLifetimeMillis;

    @Override
    public String getId() {
        return lowercaseName;
    }

    /**
    * @return true, if templateLifeTimeMillis is greater than the current time.
    */
    public boolean isValidTemplateTime() {
        return templateLifetimeMillis >= System.currentTimeMillis();
    }
}
