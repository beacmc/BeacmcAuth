package com.beacmc.beacmcauth.core.social.link;

import com.beacmc.beacmcauth.api.social.link.LinkManager;
import com.beacmc.beacmcauth.api.social.link.LinkPlayer;

import java.util.ArrayList;
import java.util.List;

public class BaseLinkManager implements LinkManager {

    private final List<LinkPlayer<?>> linkPlayers = new ArrayList<>();

    @Override
    public List<LinkPlayer<?>> getLinkPlayers() {
        return linkPlayers;
    }
}
