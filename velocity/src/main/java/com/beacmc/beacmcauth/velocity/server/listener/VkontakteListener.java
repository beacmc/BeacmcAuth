package com.beacmc.beacmcauth.velocity.server.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.button.ButtonClickRegistry;
import com.beacmc.beacmcauth.api.social.command.SocialCommandRegistry;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.social.types.vkontakte.VkontaktePlayer;
import com.ubivashka.vk.api.parsers.objects.CallbackButtonEvent;
import com.ubivashka.vk.velocity.events.VKCallbackButtonPressEvent;
import com.ubivashka.vk.velocity.events.VKMessageEvent;
import com.velocitypowered.api.event.Subscribe;
import com.vk.api.sdk.objects.messages.Message;

public class VkontakteListener {

    private final SocialCommandRegistry commands;
    private final ButtonClickRegistry buttons;
    private final BeacmcAuth plugin;

    public VkontakteListener(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.commands = plugin.getSocialManager().getSocialCommandRegistry();
        this.buttons = plugin.getSocialManager().getButtonClickRegistry();
    }

    @Subscribe
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

    @Subscribe
    public void onClick(VKCallbackButtonPressEvent event) {
        CallbackButtonEvent buttonEvent = event.getButtonEvent();
        Integer userId = buttonEvent.getUserID();
        Social<?, ?> social = plugin.getSocialManager().getSocialByType(SocialType.VKONTAKTE);
        if (userId.equals(buttonEvent.getPeerID()) && social != null && buttonEvent.getPayload() != null) {
            String data = buttonEvent.getPayload().replace("\"", "");

            Button button = Button.builder()
                    .type(ButtonType.SECONDARY)
                    .callbackData(data)
                    .label("")
                    .build();

            buttons.executeListeners(new VkontaktePlayer(plugin, buttonEvent.getPeerID()), social, button);
        }
    }
}
