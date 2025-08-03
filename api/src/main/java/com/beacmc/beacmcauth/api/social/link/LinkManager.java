package com.beacmc.beacmcauth.api.social.link;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.SocialType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public interface LinkManager {

    default <ID> void createLinkRequest(final ProtectedPlayer player, final ID id, final String code, final SocialType socialType) {
        if (player != null && code != null && id != null) {
            getLinkPlayers().add(new LinkPlayer<>(socialType, player, id, code));
        }
    }

    default List<LinkPlayer<?>> getPlayersByName(@NotNull String name) {
        return getLinkPlayers().stream()
                .filter(execute -> execute.getPlayer().getLowercaseName().equals(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    List<LinkPlayer<?>> getLinkPlayers();
}
