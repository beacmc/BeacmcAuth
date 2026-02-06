package com.beacmc.beacmcauth.api.message;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public interface Message {

    String getContentRaw();

    BaseComponent toBaseComponent();

    Component toComponent();
}
