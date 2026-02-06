package com.beacmc.beacmcauth.api.auth.premium;

import com.beacmc.beacmcauth.api.cache.CachedData;
import lombok.*;

import java.util.UUID;

/**
 * Temporary or permanent premium player
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class PremiumPlayer implements CachedData<String> {

    private final String lowercaseName;
    private UUID uniqueId;
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
