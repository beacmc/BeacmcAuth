package com.beacmc.beacmcauth.api.auth.premium.response.creator;

import com.beacmc.beacmcauth.api.auth.premium.PremiumUser;
import com.beacmc.beacmcauth.api.auth.premium.response.Response;
import com.beacmc.beacmcauth.api.config.MojangAuthConfig;
import lombok.*;
import org.jetbrains.annotations.Nullable;

@ToString
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
public class GetPremiumUserResponse implements Response<PremiumUser> {

    @ToString.Exclude
    private final MojangAuthConfig config;
    private final int statusCode;
    private final @Nullable PremiumUser data;
    private long rateLimitTime = 0L;

    @Override
    public boolean isSuccess() {
        return config.getUserFoundCode() == statusCode;
    }

    @Override
    public boolean isRateLimited() {
        return config.getRateLimitedCode() == statusCode;
    }
}
