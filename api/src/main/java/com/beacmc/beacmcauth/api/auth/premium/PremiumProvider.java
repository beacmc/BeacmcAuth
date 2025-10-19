package com.beacmc.beacmcauth.api.auth.premium;

/**
 * @param <T> T is a class or object that changes the online mode of a player
 */
public interface PremiumProvider<T> {

    /**
     * Switching a player from offline mode to online mode
     *
     * @param obj - Inheritance object T
     */
    void changeOfflineMode(T obj);
}
