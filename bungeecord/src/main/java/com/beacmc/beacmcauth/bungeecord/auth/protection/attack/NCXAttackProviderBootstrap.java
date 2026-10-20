package com.beacmc.beacmcauth.bungeecord.auth.protection.attack;

import com.beacmc.beacmcauth.api.BeacmcAuth;

public class NCXAttackProviderBootstrap {

    public static void bootstrap(BeacmcAuth plugin) {
        try {
            Class.forName("net.shieldcommunity.nullcordx.api.NullCordX");
            plugin.getProtectionManager().setAttackStatusProvider(new NCXAttackProvider(plugin));
        } catch (ClassNotFoundException ignored) {}
    }
}
