package com.beacmc.beacmcauth.api.event;

public interface EventManager {

    void register(EventListener listener);

    void unregister(EventListener listener);

    <T extends Event> void fire(T event);
}
