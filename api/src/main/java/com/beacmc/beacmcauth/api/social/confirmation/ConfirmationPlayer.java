package com.beacmc.beacmcauth.api.social.confirmation;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.event.type.AuthLoginEvent;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class ConfirmationPlayer {

    private ProtectedPlayer player;
    private List<Social<?, ?>> confirmations;
    private Social<?, ?> currentConfirmation;

    public boolean nextConfirmationSocial(BeacmcAuth plugin) {
        final int lastIndex = currentConfirmation != null ? confirmations.lastIndexOf(currentConfirmation) : -1;
        final ServerLogger logger = plugin.getServerLogger();

        if (!confirmations.isEmpty()) {
            if (currentConfirmation == null) {
                currentConfirmation = confirmations.getFirst();
                logger.debug("First confirmation(%s) for player(%s)".formatted(currentConfirmation, player.getLowercaseName()));
                return currentConfirmation != null && currentConfirmation.startConfirmation(player);
            }

            int nextIndex = lastIndex + 1;
            if (lastIndex != -1 && nextIndex < confirmations.size()) {
                Social<?, ?> social = confirmations.get(nextIndex);
                logger.debug("Next confirmation(%s) for player(%s)".formatted(social, player.getLowercaseName()));
                if (social != null && social.startConfirmation(player)) {
                    currentConfirmation = social;
                    return true;
                }
            }
        }
        ServerPlayer serverPlayer = plugin.getProxy().getPlayer(player.getUuid());

        plugin.getAuthManager().connectLobbyServer(serverPlayer);
        plugin.getAuthManager().performLogin(player);
        plugin.getEventManager().fire(new AuthLoginEvent(player, serverPlayer));
        return false;
    }
}
