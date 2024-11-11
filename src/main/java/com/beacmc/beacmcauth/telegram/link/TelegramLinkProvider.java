package com.beacmc.beacmcauth.telegram.link;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.telegram.TelegramProvider;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramLinkProvider {

    private final TelegramProvider telegramProvider;
    private final Map<ProtectedPlayer, Long> linkCodes;
    private final ProtectedPlayerDao dao;

    public TelegramLinkProvider(TelegramProvider telegramProvider) {
        this.telegramProvider = telegramProvider;
        linkCodes = new HashMap<>();
        dao = BeacmcAuth.getDatabase().getProtectedPlayerDao();
    }

    public List<ProtectedPlayer> getProtectedPlayersById(long telegram) {
        try {
            return dao.queryForEq("telegram", telegram);
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
