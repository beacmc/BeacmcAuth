package com.beacmc.beacmcauth.core.event;

import com.beacmc.beacmcauth.api.event.Event;
import com.beacmc.beacmcauth.api.event.EventListener;
import com.beacmc.beacmcauth.api.event.EventManager;

import java.util.ArrayList;
import java.util.List;

public class BaseEventManager implements EventManager {

    private final List<EventListener> listeners = new ArrayList<>();

    @Override
    public void register(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregister(EventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fire(Event event) {
        listeners.forEach(event::handle);
    }
}
