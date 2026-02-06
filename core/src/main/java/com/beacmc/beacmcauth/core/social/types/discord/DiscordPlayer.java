package com.beacmc.beacmcauth.core.social.types.discord;

import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiscordPlayer implements SocialPlayer<User, Long> {

    private final User user;

    public DiscordPlayer(@NotNull User user) {
        this.user = user;
    }

    @Override
    public void sendPrivateMessage(String message, @Nullable Object keyboard) {
        user.openPrivateChannel().flatMap(channel -> {
            MessageCreateAction action = channel.sendMessage(message);
            if (keyboard instanceof List<?> list) {
                list.forEach(object -> action.setActionRow((ItemComponent[]) object));
            }
            return action;
        }).queue();
    }

    @Override
    public Long getID() {
        return user.getIdLong();
    }

    @Override
    public boolean checkAccountLink(ProtectedPlayer player) {
        return player != null && getID() == player.getDiscord();
    }

    @Override
    public User getOriginalSocialPlayer() {
        return user;
    }
}
