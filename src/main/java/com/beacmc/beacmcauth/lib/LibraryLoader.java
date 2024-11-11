package com.beacmc.beacmcauth.lib;

import com.alessiodp.libby.BungeeLibraryManager;
import com.alessiodp.libby.Library;
import com.beacmc.beacmcauth.BeacmcAuth;

import java.util.List;

public class LibraryLoader {

    private final BungeeLibraryManager libraryManager;

    public LibraryLoader() {
        libraryManager = new BungeeLibraryManager(BeacmcAuth.getInstance());
        libraryManager.addMavenCentral();
        libraryManager.addSonatype();
        libraryManager.addJitPack();
    }

    public void loadLibrary(Library library) {
        libraryManager.loadLibrary(library);
    }

    public void loadLibraries(List<Library> libraries) {
        libraries.forEach(this::loadLibrary);
    }
}
