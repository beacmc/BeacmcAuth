package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.link.LinkManager;
import com.beacmc.beacmcauth.api.social.link.LinkPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;

import java.util.List;

public class LinkCommandExecutor implements CommandExecutor {

    private final SocialManager socialManager;
    private final LinkManager linkManager;
    private final BeacmcAuth plugin;
    private final GameCooldown cooldown;

    public LinkCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;

        socialManager = plugin.getSocialManager();
        linkManager = socialManager.getLinkManager();
        cooldown = GameCooldown.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) {
            sender.sendMessage("Only player");
            return;
        }

        final Config config = plugin.getConfig();

        if (cooldown.isCooldown(player.getLowercaseName())) {
            player.sendMessage(config.getMessages().getCooldown());
            return;
        }
        cooldown.createCooldown(player.getLowercaseName(), 10_000);

        final List<LinkPlayer<?>> linkPlayers = linkManager.getPlayersByName(player.getName());

        if (linkPlayers.isEmpty()) {
            player.sendMessage(config.getMessages().getLinkCodeAbsent());
            return;
        }

        if (args.length < 1) {
            player.sendMessage(config.getMessages().getLinkCommandUsage());
            return;
        }

        LinkPlayer<?> linkPlayer = linkPlayers.stream()
                .filter(execute -> execute.getCode().equals(args[0]))
                .findFirst()
                .orElse(null);

        if (linkPlayer == null || linkPlayer.getCode() == null) {
            player.sendMessage(config.getMessages().getIncorrectCode());
            return;
        }
        Social<?, ?> social = socialManager.getSocialByType(linkPlayer.getSocialType());

        if (social != null) {
            if (social.isPlayerLinked(linkPlayer.getPlayer())) {
                player.sendMessage(config.getMessages().getAlreadyLinked());
                return;
            }
            social.linkPlayer(linkPlayer.getPlayer(), linkPlayer.getId());
            linkManager.getLinkPlayers().remove(linkPlayer);
            player.sendMessage(config.getMessages().getLinkSuccess());
        }
    }
}
