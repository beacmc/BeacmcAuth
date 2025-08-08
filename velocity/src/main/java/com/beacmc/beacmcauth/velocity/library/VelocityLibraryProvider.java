package com.beacmc.beacmcauth.velocity.library;

import com.alessiodp.libby.VelocityLibraryManager;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;

public class VelocityLibraryProvider extends VelocityLibraryManager<VelocityBeacmcAuth> implements LibraryProvider {

    public VelocityLibraryProvider(VelocityBeacmcAuth velocityAuth) {
        super(velocityAuth, velocityAuth.getVelocityLogger(), velocityAuth.getDataDirectory(), velocityAuth.getVelocityProxyServer().getPluginManager());
        addMavenCentral();
        addSonatype();
        addJCenter();
        addJitPack();
    }
}
