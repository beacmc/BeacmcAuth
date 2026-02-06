package com.beacmc.beacmcauth.api.social;

import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.command.SocialCommandRegistry;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickRegistry;
import com.beacmc.beacmcauth.api.social.link.LinkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SocialManager {

    @NotNull List<ConfirmationPlayer> getConfirmationPlayers();

    @NotNull List<Social<?, ?>> getSocials();

    boolean startPlayerConfirmations(ProtectedPlayer player);

    default @Nullable Social<?, ?> getSocialByType(SocialType type) {
        return getSocials().stream()
                .filter(social -> social.getType() == type)
                .findFirst()
                .orElse(null);
    }

    @NotNull List<Social<?, ?>> getPlayerLinkedSocials(ProtectedPlayer player);

    default @Nullable ConfirmationPlayer getConfirmationByPlayer(ProtectedPlayer player) {
        return getConfirmationPlayers().stream()
                .filter(execute -> execute != null && execute.getPlayer() == player)
                .findFirst()
                .orElse(null);
    }

    default @Nullable ConfirmationPlayer getConfirmationByName(String name) {
        return getConfirmationPlayers().stream()
                .filter(execute -> execute != null && execute.getPlayer().getLowercaseName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    ButtonClickRegistry getButtonClickRegistry();

    SocialCommandRegistry getSocialCommandRegistry();

    LinkManager getLinkManager();
}