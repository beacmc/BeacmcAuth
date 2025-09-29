package com.beacmc.beacmcauth.core.social.base.button;

import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickListener;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickRegistry;

import java.util.ArrayList;
import java.util.List;

public class BaseButtonClickRegistry implements ButtonClickRegistry {

    private final List<ButtonClickListener> listeners = new ArrayList<>();

    @Override
    public List<ButtonClickListener> getListeners() {
        return listeners;
    }
}
