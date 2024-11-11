package com.beacmc.beacmcauth.discord.command;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.discord.link.DiscordLinkProvider;
import com.beacmc.beacmcauth.util.CodeGenerator;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

public class LinkCommand extends ListenerAdapter {

    private final DiscordProvider discordProvider;
    private final DiscordLinkProvider linkProvider;

    public LinkCommand(DiscordProvider discordProvider) {
        this.discordProvider = discordProvider;
        this.linkProvider = discordProvider.getLinkProvider();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        final User user = event.getAuthor();
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final Message message = event.getMessage();
        final MessageChannelUnion channel = event.getChannel();
        final String[] args = message.getContentRaw().split("\\s+");

        if (!message.getContentRaw().startsWith(discordConfig.getLinkCommand()))
            return;

        if (!(channel instanceof PrivateChannel)) {
            channel.sendMessage(discordConfig.getMessage("private-channel-only")).queue();
            return;
        }

        if (args.length < 2) {
            channel.sendMessage(discordConfig.getMessage("link-command-usage")).queue();
            return;
        }

        String name = args[1].toLowerCase();
        ProtectedPlayer.get(name).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                channel.sendMessage(discordConfig.getMessage("link-command-player-not-found")).queue();
                return;
            }

            ProxiedPlayer player = protectedPlayer.getPlayer();

            if (player == null) {
                channel.sendMessage(discordConfig.getMessage("link-command-player-offline")).queue();
                return;
            }

            if (protectedPlayer.getDiscord() != 0) {
                channel.sendMessage(discordConfig.getMessage("link-command-already-linked")).queue();
                return;
            }

            if (linkProvider.getProtectedPlayersById(user.getIdLong()).size() < discordConfig.getMaxLink()) {
                String code = CodeGenerator.generate(discordConfig.getCodeChars(), discordConfig.getCodeLength());
                linkProvider.getLinkCodes().put(protectedPlayer.setLinkCode(code), user.getIdLong());
                channel.sendMessage(discordConfig.getMessage("link-message", Map.of("%name%", player.getDisplayName(), "%code%", code))).queue();
                return;
            }
            channel.sendMessage(discordConfig.getMessage("link-limit")).queue();
        });
    }
}
