package com.beacmc.beacmcauth.core.social.base.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.command.SocialCommand;
import com.beacmc.beacmcauth.core.util.CodeGenerator;

import java.util.Map;

public class LinkCommand implements SocialCommand {

    private final BeacmcAuth plugin;

    public LinkCommand(BeacmcAuth plugin) {
        this.plugin = plugin;
    }

    public void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String prefix, String[] args) {
        if (!social.getLinkCommandPrefix().equals(prefix)) return;

        SocialConfig socialConfig = social.getSocialConfig();

        if (args.length < 1) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getLinkCommandUsage());
            return;
        }

        if (social.isCooldown(socialPlayer)) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getCooldown());
            return;
        }
        social.createCooldown(socialPlayer.getID(), 6000L);

        String playerName = args[0].toLowerCase();
        plugin.getAuthManager().getProtectedPlayer(playerName).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getLinkCommandPlayerNotFound());
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (player == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getLinkCommandPlayerOffline());
                return;
            }

            if (social.isPlayerLinked(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getLinkCommandAlreadyLinked());
                return;
            }

            if (social.getLinkedPlayersById(socialPlayer.getID()).size() < socialConfig.getMaxLink()) {
                String code = CodeGenerator.generate(socialConfig.getCodeChars(), socialConfig.getCodeLength());

                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getLinkMessage()
                        .replace("%name%", player.getName())
                        .replace("%code%", code));
                plugin.getSocialManager().getLinkManager().createLinkRequest(protectedPlayer, socialPlayer.getID(), code, social.getType());
                return;
            }
            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getLinkLimit());
        });
    }
}
