package com.beacmc.beacmcauth.api.library;

import com.alessiodp.libby.Library;

public interface LibraryProvider {

    void loadLibrary(Library library);

    void loadLibraries(Library... libraries);

    void addRepository(String url);
}
