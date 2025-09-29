package com.beacmc.beacmcauth.bungeecord.server.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickRegistry;
import com.beacmc.beacmcauth.api.social.command.SocialCommandRegistry;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.social.types.vkontakte.VkontaktePlayer;
import com.beacmc.beacmcauth.core.social.types.vkontakte.VkontakteSocial;
import com.ubivashka.vk.api.parsers.objects.CallbackButtonEvent;
import com.ubivashka.vk.bungee.events.VKCallbackButtonPressEvent;
import com.ubivashka.vk.bungee.events.VKMessageEvent;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class VkontakteListener implements Listener {

    private final SocialCommandRegistry commands;
    private final ButtonClickRegistry buttons;
    private final BeacmcAuth plugin;

    public VkontakteListener(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.commands = plugin.getSocialManager().getSocialCommandRegistry();
        this.buttons = plugin.getSocialManager().getButtonClickRegistry();
    }

    @EventHandler
    public void onMessage(VKMessageEvent event) {
        if (event.getPeer().equals(event.getUserId())) {
            Message message = event.getMessage();
            String text = message.getText();
            Social<?, ?> social = plugin.getSocialManager().getSocialByType(SocialType.VKONTAKTE);
            if (text != null && !text.isEmpty() && social != null) {
                String[] args = text.split("\\s+");
                if (args.length > 0) {
                    String replacedText = text.replace(args[0], "").trim();
                    commands.executeCommands(new VkontaktePlayer(plugin, event.getPeer()), social, args[0], replacedText.split("\\s+"));
                }
            }
        }
    }

    @EventHandler
    public void onClick(VKCallbackButtonPressEvent event) {
        CallbackButtonEvent buttonEvent = event.getButtonEvent();
        Integer userId = buttonEvent.getUserID();
        VkontakteSocial social = (VkontakteSocial) plugin.getSocialManager().getSocialByType(SocialType.VKONTAKTE);
        if (userId.equals(buttonEvent.getPeerID()) && social != null && buttonEvent.getPayload() != null) {
            String data = buttonEvent.getPayload().replace("\"", "");

            Button button = Button.builder()
                    .type(ButtonType.SECONDARY)
                    .callbackData(data)
                    .label("")
                    .build();

            try {
                social.getVkApiPlugin().getVkApiProvider().getVkApiClient().messages()
                        .sendMessageEventAnswer(social.getVkApiPlugin().getVkApiProvider().getActor(), buttonEvent.getEventID(), buttonEvent.getUserID(), buttonEvent.getPeerID())
                        .execute();
            } catch (ClientException | ApiException ignored) { }

            buttons.executeListeners(new VkontaktePlayer(plugin, buttonEvent.getPeerID()), social, button);
        }
    }
}
