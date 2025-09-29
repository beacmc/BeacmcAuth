package com.beacmc.beacmcauth.bungeecord.library;

import com.alessiodp.libby.BungeeLibraryManager;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;

public class BungeeLibraryProvider extends BungeeLibraryManager implements LibraryProvider {


    public BungeeLibraryProvider(BungeeBeacmcAuth plugin) {
        super(plugin);
        addMavenCentral();
        addJitPack();
        addSonatype();
    }
}
