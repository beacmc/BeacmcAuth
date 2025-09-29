package com.beacmc.beacmcauth.velocity.auth;

import com.beacmc.beacmcauth.api.auth.premium.PremiumProvider;
import com.velocitypowered.api.event.connection.PreLoginEvent;

public class VelocityPremiumProvider implements PremiumProvider<PreLoginEvent> {

    @Override
    public void changeOfflineMode(PreLoginEvent event) {
        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
    }
}
