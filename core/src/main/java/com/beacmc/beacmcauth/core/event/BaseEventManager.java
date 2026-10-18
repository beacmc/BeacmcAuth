package com.beacmc.beacmcauth.core.event;

import com.beacmc.beacmcauth.api.event.Event;
import com.beacmc.beacmcauth.api.event.EventListener;
import com.beacmc.beacmcauth.api.event.EventManager;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BaseEventManager implements EventManager {

    private final ServerLogger logger;
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
        logger.debug("Fire event %s".formatted(event));
        listeners.forEach(event::handle);
    }
}
