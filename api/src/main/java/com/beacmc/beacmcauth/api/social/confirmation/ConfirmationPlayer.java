package com.beacmc.beacmcauth.api.social.confirmation;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.social.Social;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ConfirmationPlayer {

    ProtectedPlayer player;
    List<Social<?, ?>> confirmations;
    Social<?, ?> currentConfirmation;

    public boolean nextConfirmationSocial(BeacmcAuth plugin) {
        final int lastIndex = currentConfirmation != null ? confirmations.lastIndexOf(currentConfirmation) : -1;
        final ServerLogger logger = plugin.getServerLogger();

        if (!confirmations.isEmpty()) {
            if (currentConfirmation == null) {
                currentConfirmation = confirmations.get(0);
                logger.debug("First confirmation(%s) for player(%s)".formatted(confirmations.get(0), player.getLowercaseName()));
                return currentConfirmation != null && currentConfirmation.startConfirmation(player);
            }

            if (lastIndex != -1 && lastIndex + 1 < confirmations.size()) {
                Social<?, ?> social = confirmations.get(lastIndex + 1);
                logger.debug("Next confirmation(%s) for player(%s)".formatted(social, player.getLowercaseName()));
                if (social != null && social.startConfirmation(player)) {
                    currentConfirmation = social;
                    return true;
                }
            }
        }
        plugin.getAuthManager().connectGameServer(plugin.getProxy().getPlayer(player.getUuid()));
        plugin.getAuthManager().performLogin(player);
        return false;
    }
}
