package com.beacmc.beacmcauth.core.social.types.discord.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.social.types.discord.DiscordPlayer;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class DiscordListener extends ListenerAdapter {

    private final BeacmcAuth plugin;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!(event.getChannel() instanceof PrivateChannel)) return;

        final String message = event.getMessage().getContentRaw();
        final Social<?, ?> discord = plugin.getSocialManager().getSocialByType(SocialType.DISCORD);

        if (!message.isEmpty()) {
            String[] args = message.split("\\s+");
            if (args.length > 0) {
                String replacedText = message.replace(args[0], "").trim();
                plugin.getSocialManager().getSocialCommandRegistry().executeCommands(
                        new DiscordPlayer(event.getAuthor()),
                        discord,
                        args[0],
                        replacedText.split("\\s+")
                );
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final Social<?, ?> discord = plugin.getSocialManager().getSocialByType(SocialType.DISCORD);

        plugin.getSocialManager().getButtonClickRegistry().executeListeners(
                new DiscordPlayer(event.getUser()),
                discord,
                Button.builder()
                        .callbackData(event.getButton().getId())
                        .label(event.getButton().getLabel())
                        .type(ButtonType.SECONDARY)
                        .build()
        );
    }
}
