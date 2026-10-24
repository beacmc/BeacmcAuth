package com.beacmc.beacmcauth.api.config;

public interface Argon2Settings {

    int getIterations();

    int getMemoryKb();

    int getParallelism();
}
