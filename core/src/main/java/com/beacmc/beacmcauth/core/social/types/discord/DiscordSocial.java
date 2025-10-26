package com.beacmc.beacmcauth.core.social.types.discord;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;
import com.beacmc.beacmcauth.api.config.social.DiscordConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.cache.cooldown.DiscordCooldown;
import com.beacmc.beacmcauth.core.social.types.discord.listener.DiscordListener;
import com.beacmc.beacmcauth.core.util.runnable.DiscordRunnable;
import lombok.ToString;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ToString
public class DiscordSocial implements Social<JDA, Long> {

    private final BeacmcAuth plugin;
    private final ServerLogger logger;

    private JDA jda;
    private Guild guild;

    public DiscordSocial(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
        try {
            JDALogger.setFallbackLoggerEnabled(false);
            this.jda = JDABuilder.createDefault(plugin.getDiscordConfig().getToken(), Arrays.asList(GatewayIntent.values()))
                    .addEventListeners(new DiscordListener(plugin))
                    .build()
                    .awaitReady();

            if (getSocialConfig().isActivityEnabled()) {
                jda.getPresence().setActivity(Activity.of(
                        plugin.getDiscordConfig().getActivityType(),
                        plugin.getDiscordConfig().getActivityText(),
                        plugin.getDiscordConfig().getActivityUrl()
                ));
            }
            this.guild = jda.getGuildById(plugin.getDiscordConfig().getGuildID());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isEnabled() {
        return plugin.getDiscordConfig().isEnabled();
    }

    @Override
    public boolean isInit() {
        return isEnabled();
    }

    @Override
    public Object createKeyboard(Keyboard keyboard) {
        List<ItemComponent[]> items = new ArrayList<>();
        keyboard.getButtons().forEach(buttons -> {
            List<ItemComponent> row = new ArrayList<>();
            buttons.forEach(button -> row.add(Button.of(getButtonStyle(button.getType()), button.getCallbackData(), button.getLabel())));
            items.add(row.toArray(ItemComponent[]::new));
        });
        return items;
    }

    @Override
    public JDA getOriginalSocial() {
        return jda;
    }

    @Override
    public SocialType getType() {
        return SocialType.DISCORD;
    }

    @Override
    public boolean isPlayerTwoFaEnabled(ProtectedPlayer player) {
        return isPlayerLinked(player) && player.isDiscordTwoFaEnabled();
    }

    @Override
    public void switchPlayerTwoFa(ProtectedPlayer player, boolean enabled) {
        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            player.setDiscordTwoFaEnabled(enabled);
            dao.createOrUpdate(player);
        } catch (SQLException e) {
            logger.error("DiscordSocial#switchPlayerTwoFa have SQLException: " + e.getMessage());
        }
    }

    @Override
    public boolean isPlayerLinked(ProtectedPlayer player) {
        return player.getDiscord() != 0;
    }

    @Override
    public void linkPlayer(ProtectedPlayer player, Object id) {
        if (player.getDiscord() != 0 || !(id instanceof Long longId)) return;

        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            player.setDiscord(longId);
            dao.createOrUpdate(player);
        } catch (SQLException e) {
            logger.error("DiscordSocial#linkPlayer have SQLException: " + e.getMessage());
        }
    }

    @Override
    public void unlinkPlayer(ProtectedPlayer player) {
        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            player.setDiscord(0);
            dao.createOrUpdate(player);
        } catch (SQLException e) {
            logger.error("DiscordSocial#unlinkPlayer have SQLException: " + e.getMessage());
        }
    }

    @Override
    public String getLinkCommandPrefix() {
        return plugin.getDiscordConfig().getLinkCommand();
    }

    @Override
    public DiscordConfig getSocialConfig() {
        return plugin.getDiscordConfig();
    }

    @Override
    public AbstractCooldown<Long> getCooldownCache() {
        return DiscordCooldown.getInstance();
    }

    @Override
    public boolean isCooldown(Object id) {
        if (!(id instanceof Long longId))
            return false;

        return getCooldownCache().isCooldown(longId);
    }

    @Override
    public void createCooldown(Object id, long time) {
        if (id instanceof Long longId) {
            getCooldownCache().createCooldown(longId, time);
        }
    }

    @Override
    public boolean startConfirmation(ProtectedPlayer player) {
        SocialManager manager = plugin.getSocialManager();
        ConfirmationPlayer confirmationPlayer = manager.getConfirmationByPlayer(player);
        ServerPlayer serverPlayer = plugin.getProxy().getPlayer(player.getLowercaseName());
        User user = jda.getUserById(player.getDiscord());

        if (serverPlayer == null) {
            logger.debug("ServerPlayer for name(%s) is null".formatted(player.getLowercaseName()));
            return false;
        }

        if (confirmationPlayer == null || player.getDiscord() == 0 || user == null) {
            serverPlayer.disconnect(plugin.getConfig().getMessages().getInternalError());
            return false;
        }

        new DiscordRunnable(plugin, serverPlayer, player);
        String message = getSocialConfig().getMessages().getConfirmationMessage()
                .replace("%name%", player.getLowercaseName())
                .replace("%ip%", serverPlayer.getInetAddress().getHostAddress());

        DiscordPlayer discordPlayer = new DiscordPlayer(user);
        
        Object objectKeyboard = createKeyboard(getSocialConfig().getKeyboards().createConfirmationKeyboard(player));
        discordPlayer.sendPrivateMessage(message, objectKeyboard);
        return true;
    }

    @Override
    public List<ProtectedPlayer> getLinkedPlayersById(Object id) {
        if (!(id instanceof Long))
            return Collections.emptyList();

        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            return dao.queryForEq("discord", id);
        } catch (SQLException e) {
            logger.error("DiscordSocial#getLinkedPlayersById have SQLException: " + e.getMessage());
        }
        return null;
    }

    private ButtonStyle getButtonStyle(ButtonType type) {
        return switch (type) {
            case DANGER -> ButtonStyle.DANGER;
            case PRIMARY -> ButtonStyle.PRIMARY;
            case SUCCESS -> ButtonStyle.SUCCESS;
            default -> ButtonStyle.SECONDARY;
        };
    }
}
