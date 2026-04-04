package com.beacmc.beacmcauth.api.auth;

import com.beacmc.beacmcauth.api.cache.CachedData;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(chain = true)
public class AuthenticatingPlayer implements CachedData<String> {

    private final ProtectedPlayer player;
    private int attempts;

    @Override
    public String getId() {
        return player.getLowercaseName();
    }

    public boolean useAttempt() {
        return (attempts -= 1) > 0;
    }
}
