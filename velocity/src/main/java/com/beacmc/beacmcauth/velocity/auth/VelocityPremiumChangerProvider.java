package com.beacmc.beacmcauth.velocity.auth;

import com.beacmc.beacmcauth.api.auth.premium.mojang.PremiumChangerProvider;
import com.velocitypowered.api.event.connection.PreLoginEvent;

public class VelocityPremiumChangerProvider implements PremiumChangerProvider<PreLoginEvent> {

    @Override
    public void forceOfflineMode(PreLoginEvent event) {
        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
    }

    @Override
    public void forceOnlineMode(PreLoginEvent event) {
        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
    }
}
