package com.beacmc.beacmcauth.core.social.base.button;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickListener;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConfirmationButtonClick implements ButtonClickListener {

    private final BeacmcAuth plugin;

    @Override
    public void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, Button button) {
        String[] args = button.getCallbackData().split(":");
        if (args.length < 2) return;

        final String id = args[1];
        final SocialManager manager = plugin.getSocialManager();
        final SocialConfig socialConfig = social.getSocialConfig();
        final Config config = plugin.getConfig();

        if (args[0].equals("confirm-accept")) {

            ConfirmationPlayer confirmationPlayer = manager.getConfirmationByName(id);
            if (confirmationPlayer == null
                    || confirmationPlayer.getCurrentConfirmation() == null
                    || confirmationPlayer.getCurrentConfirmation().getType() != social.getType())
            {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getNoConfirmation());
                return;
            }

            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getConfirmationSuccess());
            if (!confirmationPlayer.nextConfirmationSocial(plugin)) {
                manager.getConfirmationPlayers().remove(confirmationPlayer);
            }
        }

        if (args[0].equals("confirm-decline")) {
            ConfirmationPlayer confirmationPlayer = manager.getConfirmationByName(id);
            if (confirmationPlayer == null
                    || confirmationPlayer.getCurrentConfirmation() == null
                    || confirmationPlayer.getCurrentConfirmation().getType() != social.getType())
            {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getNoConfirmation());
                return;
            }

            ProtectedPlayer protectedPlayer = confirmationPlayer.getPlayer();
            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getConfirmationDenied());

            if (player != null) {
                ConfigMessages messages = config.getMessages();
                String disconnectMessage = switch (social.getType()) {
                    case DISCORD ->  messages.getDiscordConfirmationDeniedDisconnect();
                    case TELEGRAM -> messages.getTelegramConfirmationDeniedDisconnect();
                    case VKONTAKTE -> messages.getVkontakteConfirmationDeniedDisconnect();
                    case CUSTOM -> null;
                };
                player.disconnect(disconnectMessage);
            }
        }
    }
}
