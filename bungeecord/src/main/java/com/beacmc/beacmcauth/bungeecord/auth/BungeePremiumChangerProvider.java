package com.beacmc.beacmcauth.bungeecord.auth;

import com.beacmc.beacmcauth.api.auth.premium.mojang.PremiumChangerProvider;
import net.md_5.bungee.api.connection.PendingConnection;

public class BungeePremiumChangerProvider implements PremiumChangerProvider<PendingConnection> {

    @Override
    public void forceOfflineMode(PendingConnection pendingConnection) {
        pendingConnection.setOnlineMode(false);
    }

    @Override
    public void forceOnlineMode(PendingConnection pendingConnection) {
        pendingConnection.setOnlineMode(true);
    }
}
