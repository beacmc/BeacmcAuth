package com.beacmc.beacmcauth.discord.link;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.discord.DiscordProvider;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordLinkProvider {

    private final DiscordProvider discordProvider;
    private final Map<ProtectedPlayer, Long> linkCodes;
    private final ProtectedPlayerDao dao;

    public DiscordLinkProvider(DiscordProvider discordProvider) {
        this.discordProvider = discordProvider;
        linkCodes = new HashMap<>();
        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
    }

    public List<ProtectedPlayer> getProtectedPlayersById(long discord) {
        try {
            return dao.queryForEq("discord", discord);
        } catch (SQLException e) {
        }
        return Collections.emptyList();
    }

    public ProtectedPlayer getPlayerByName(String name) {
        return linkCodes.keySet().stream()
                .filter(p -> p.getLowercaseName().equals(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public Map<ProtectedPlayer, Long> getLinkCodes() {
        return linkCodes;
    }
}
