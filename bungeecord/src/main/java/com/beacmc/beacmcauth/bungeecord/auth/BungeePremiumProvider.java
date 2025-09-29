package com.beacmc.beacmcauth.bungeecord.auth;

import com.beacmc.beacmcauth.api.auth.premium.PremiumProvider;
import net.md_5.bungee.api.connection.PendingConnection;

public class BungeePremiumProvider implements PremiumProvider<PendingConnection> {

    @Override
    public void changeOfflineMode(PendingConnection pendingConnection) {
        pendingConnection.setOnlineMode(true);
    }
}
