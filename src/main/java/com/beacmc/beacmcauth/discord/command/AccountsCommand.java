package com.beacmc.beacmcauth.discord.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.discord.link.DiscordLinkProvider;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AccountsCommand extends ListenerAdapter {

    private final DiscordProvider discordProvider;
    private final DiscordLinkProvider linkProvider;

    public AccountsCommand(DiscordProvider discordProvider) {
        this.discordProvider = discordProvider;
        this.linkProvider = discordProvider.getLinkProvider();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        final User user = event.getAuthor();
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final Message message = event.getMessage();
        final MessageChannelUnion channel = event.getChannel();

        if (!message.getContentRaw().startsWith(discordConfig.getAccountsCommand()))
            return;

        if (!(channel instanceof PrivateChannel)) {
            channel.sendMessage(discordConfig.getMessage("private-channel-only", Map.of())).queue();
            return;
        }

        List<ProtectedPlayer> players = linkProvider.getProtectedPlayersById(user.getIdLong());
        if (players.size() < 1) {
            channel.sendMessage(discordConfig.getMessage("accounts-empty", Map.of())).queue();
            return;
        }

        sendPlayerButtons(channel, players, 0);
    }

    private void sendPlayerButtons(MessageChannelUnion channel, List<ProtectedPlayer> players, int page) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        int start = page * 4;
        int end = Math.min(start + 4, players.size());
        List<ProtectedPlayer> pageContent = players.subList(start, end);

        if (pageContent.size() < 1) return;

        MessageCreateAction channelMessage = channel.sendMessage(discordConfig.getMessage("choose-account"));
        for (ProtectedPlayer player : pageContent) {
            channelMessage.addActionRow(Button.primary("account:" + player.getLowercaseName(), player.getRealName()));
        }

        Button previousButton = Button.secondary("previous:" + page, "⏪").asDisabled();
        Button nextButton = Button.secondary("next:" + page, "⏩");

        if (page > 0) previousButton = previousButton.asEnabled();
        if (end < players.size()) nextButton = nextButton.asEnabled();
        else nextButton = nextButton.asDisabled();

        channelMessage.addActionRow(previousButton, nextButton);
        channelMessage.queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final String[] split = event.getComponentId().split(":");

        if (split.length != 2) return;

        String action = split[0];
        String id = split[1];

        if (action.equals("account")) {
            CompletableFuture<ProtectedPlayer> completableFuture = ProtectedPlayer.get(id.toLowerCase());
            completableFuture.thenAccept(protectedPlayer -> {
                if (protectedPlayer == null) {
                    System.out.println("Account is null");
                    return;
                }

                if (protectedPlayer.getDiscord() != event.getUser().getIdLong()) {
                    event.reply(discordConfig.getMessage("account-not-linked"))
                            .setEphemeral(true)
                            .queue();
                    return;
                }


                String playerOnline = discordConfig.getMessage("player-info-online");
                String playerOffline = discordConfig.getMessage("player-info-offline");
                String message = discordConfig.getMessage("account-info", Map.of(
                                "%name%", protectedPlayer.getRealName(),
                                "%lowercase_name%", protectedPlayer.getLowercaseName(),
                                "%last_ip%", protectedPlayer.getLastIp(),
                                "%reg_ip%", protectedPlayer.getRegisterIp(),
                                "%is_online%", protectedPlayer.getPlayer() == null ? playerOffline : playerOnline
                        )
                );

                ReplyCallbackAction reply = event.reply(message)
                        .addActionRow(
                                Button.secondary("toggle-2fa:" + id, discordConfig.getMessage("account-2fa-toggle-button")),
                                Button.secondary("reset-password:" + id, discordConfig.getMessage("account-reset-password-button"))
                        )
                        .addActionRow(
                                Button.secondary("toggle-ban:" + id, discordConfig.getMessage("account-ban-toggle-button")),
                                Button.secondary("kick:" + id, discordConfig.getMessage("account-kick-button"))
                        );
                if (!discordConfig.isDisableUnlink()) {
                    reply.addActionRow(Button.danger("unlink:" + id, discordConfig.getMessage("account-unlink-button")));
                }
                reply.setEphemeral(true).queue();
            });
        } else if (action.equals("previous") || action.equals("next")) {
            int currentPage = Integer.parseInt(id);
            int newPage = action.equals("next") ? currentPage + 1 : currentPage - 1;

            long discordId = event.getUser().getIdLong();
            List<ProtectedPlayer> players = linkProvider.getProtectedPlayersById(discordId);
            sendPlayerButtons(event.getChannel(), players, newPage);

            event.deferEdit().queue();
        }
    }
}
