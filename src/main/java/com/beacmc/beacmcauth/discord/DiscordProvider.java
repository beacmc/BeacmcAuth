package com.beacmc.beacmcauth.discord;

import com.beacmc.beacmcauth.BeacmcAuth;
import com.beacmc.beacmcauth.ProtectedPlayer;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.discord.command.AccountsCommand;
import com.beacmc.beacmcauth.discord.command.LinkCommand;
import com.beacmc.beacmcauth.discord.link.DiscordLinkProvider;
import com.beacmc.beacmcauth.discord.listener.AccountListener;
import com.beacmc.beacmcauth.discord.listener.ConfirmationListener;
import com.beacmc.beacmcauth.lib.Libraries;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DiscordProvider {

    private JDA jda;
    private Guild guild;
    private final Logger logger;
    private final BeacmcAuth plugin;
    private final DiscordLinkProvider linkProvider;
    private final Map<String, ProtectedPlayer> confirmationUsers;

    public DiscordProvider() {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();

        confirmationUsers = new HashMap<>();
        plugin = BeacmcAuth.getInstance();
        logger = plugin.getLogger();
        linkProvider = new DiscordLinkProvider(this);
        if (discordConfig.isEnabled()) init();
    }

    public void init() {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();

        BeacmcAuth.getLibraryLoader().loadLibrary(Libraries.JDA);
        logger.info("[Discord] The bot turning on...");

        jda = JDABuilder.createDefault(discordConfig.getToken(), GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.SCHEDULED_EVENTS).build();
        if (!jda.getStatus().isInit()) {
            logger.severe("[Discord] Bot is not initializing.");
            return;
        }

        guild = jda.getGuildById(discordConfig.getGuildID());
        if (guild == null) {
            logger.warning("[Discord] Guild not found.");
        }

        jda.addEventListener(new ConfirmationListener(this), new AccountListener());
        jda.addEventListener(new LinkCommand(this), new AccountsCommand(this));

        if (discordConfig.isActivityEnabled()) {
            jda.getPresence().setActivity(Activity.of(getActivityType(discordConfig.getActivityType()), discordConfig.getActivityText(), discordConfig.getActivityUrl()));
        }

        logger.info("[Discord] The bot has been successfully switched on");
    }

    public void sendConfirmationMessage(ProtectedPlayer protectedPlayer) {
        final DiscordConfig discordConfig = BeacmcAuth.getDiscordConfig();
        final ProxiedPlayer player = protectedPlayer.getPlayer();
        final long discord = protectedPlayer.getDiscord();

        if (discord == 0 || player == null)
            return;

        User user = jda.getUserById(discord);
        if (user == null) {
            logger.severe("User " + discord + " not found!");
            return;
        }

        user.openPrivateChannel().flatMap(channel -> {
            String ip = player.getAddress().getHostName();
            return channel.sendMessage(discordConfig.getMessage("confirmation-message", Map.of("%name%", player.getDisplayName(), "%ip%", ip)))
                    .setActionRow(
                            Button.success("confirm-accept-" + protectedPlayer.getLowercaseName(), discordConfig.getMessage("confirmation-button-accept-text", Map.of())),
                            Button.danger("confirm-decline-" + protectedPlayer.getLowercaseName(), discordConfig.getMessage("confirmation-button-decline-text", Map.of()))
                    );
        }).queue();
    }

    public Map<String, ProtectedPlayer> getConfirmationUsers() {
        return confirmationUsers;
    }

    public boolean isEnabled() {
        if (jda == null)
            return false;
        return jda.getStatus().isInit();
    }

    private Activity.ActivityType getActivityType(String activityType) {
        try {
            return Activity.ActivityType.valueOf(activityType.toUpperCase());
        } catch (IllegalArgumentException e) {
        }
        return Activity.ActivityType.PLAYING;
    }

    public DiscordLinkProvider getLinkProvider() {
        return linkProvider;
    }
}
