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
    private final ProtectedPlayerDao dao;

    public LinkCommand(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.dao = plugin.getDatabase().getProtectedPlayerDao();
    }

    public void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String prefix, String[] args) {
        if (!social.getLinkCommandPrefix().equals(prefix)) return;

        SocialConfig socialConfig = social.getSocialConfig();

        if (args.length < 1) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessage("link-command-usage"));
            return;
        }

        if (social.isCooldown(socialPlayer)) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessage("cooldown"));
            return;
        }
        social.createCooldown(socialPlayer.getID(), 6000L);

        String playerName = args[0].toLowerCase();
        plugin.getAuthManager().getProtectedPlayer(playerName).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("link-command-player-not-found"));
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (player == null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("link-command-player-offline"));
                return;
            }

            if (social.isPlayerLinked(protectedPlayer)) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessage("link-command-already-linked"));
                return;
            }

            if (social.getLinkedPlayersById(socialPlayer.getID()).size() < socialConfig.getMaxLink()) {
                String code = CodeGenerator.generate(socialConfig.getCodeChars(), socialConfig.getCodeLength());

                socialPlayer.sendPrivateMessage(socialConfig.getMessage("link-message", Map.of("%name%", player.getName(), "%code%", code)));
                plugin.getSocialManager().getLinkManager().createLinkRequest(protectedPlayer, socialPlayer.getID(), code, social.getType());
                return;
            }
            socialPlayer.sendPrivateMessage(socialConfig.getMessage("link-limit"));
        });
    }
}
