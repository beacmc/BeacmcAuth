package com.beacmc.beacmcauth.core.social.types.telegram;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.config.social.TelegramConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.cache.cooldown.TelegramCooldown;
import com.beacmc.beacmcauth.core.social.types.telegram.listener.TelegramUpdatesListener;
import com.beacmc.beacmcauth.core.util.runnable.TelegramRunnable;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.ToString;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ToString
public class TelegramSocial implements Social<TelegramBot, Long> {

    private final BeacmcAuth plugin;
    private final TelegramBot telegramBot;
    private final ServerLogger logger;

    public TelegramSocial(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
        this.telegramBot = new TelegramBot(plugin.getTelegramConfig().getToken());
        this.telegramBot.setUpdatesListener(new TelegramUpdatesListener(plugin));
    }

    @Override
    public boolean isEnabled() {
        return plugin.getTelegramConfig().isEnabled();
    }

    @Override
    public boolean isInit() {
        return isEnabled();
    }

    @Override
    public Object createKeyboard(Keyboard keyboard) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        keyboard.getButtons().forEach(lines -> {
            List<InlineKeyboardButton> line = new LinkedList<>();
            lines.forEach(button ->
                    line.add(new InlineKeyboardButton(button.getLabel())
                            .callbackData(button.getCallbackData()))
            );
            markup.addRow(line.toArray(new InlineKeyboardButton[0]));
        });
        return markup;
    }

    @Override
    public TelegramBot getOriginalSocial() {
        return telegramBot;
    }

    @Override
    public SocialType getType() {
        return SocialType.TELEGRAM;
    }

    @Override
    public boolean isPlayerTwoFaEnabled(ProtectedPlayer player) {
        return isPlayerLinked(player) && player.isTelegramTwoFaEnabled();
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
    public void switchPlayerTwoFa(ProtectedPlayer player, boolean val) {
        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            player.setTelegramTwoFaEnabled(val);
            dao.createOrUpdate(player);
        } catch (SQLException e) {
            logger.error("TelegramSocial#switchPlayerTwoFa have SQLException: " + e.getMessage());
        }
    }

    @Override
    public boolean isPlayerLinked(ProtectedPlayer player) {
        return player.getTelegram() != 0;
    }

    @Override
    public String getLinkCommandPrefix() {
        return plugin.getTelegramConfig().getLinkCommand();
    }

    @Override
    public void linkPlayer(ProtectedPlayer player, Object id) {
        if (player.getTelegram() != 0 || !(id instanceof Long longId)) return;

        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            player.setTelegram(longId);
            dao.createOrUpdate(player);
        } catch (SQLException e) {
            logger.error("TelegramSocial#linkPlayer have SQLException: " + e.getMessage());
        }
    }

    @Override
    public void unlinkPlayer(ProtectedPlayer player) {
        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            player.setTelegram(0);
            dao.createOrUpdate(player);
        } catch (SQLException e) {
            logger.error("TelegramSocial#unlinkPlayer have SQLException: " + e.getMessage());
        }
    }

    @Override
    public AbstractCooldown<Long> getCooldownCache() {
        return TelegramCooldown.getInstance();
    }

    @Override
    public boolean startConfirmation(ProtectedPlayer player) {
        SocialManager manager = plugin.getSocialManager();
        ConfirmationPlayer confirmationPlayer = manager.getConfirmationByPlayer(player);
        ServerPlayer serverPlayer = plugin.getProxy().getPlayer(player.getLowercaseName());

        if (serverPlayer == null) {
            logger.debug("ServerPlayer for name(%s) is null".formatted(player.getLowercaseName()));
            return false;
        }

        if (confirmationPlayer == null || player.getTelegram() == 0) {
            serverPlayer.disconnect(plugin.getConfig().getMessages().getInternalError());
            return false;
        }

        new TelegramRunnable(plugin, serverPlayer, player);
        SendMessage sendMessage = new SendMessage(player.getTelegram(), getSocialConfig().getMessages().getConfirmationMessage()
                .replace("%name%", player.getLowercaseName())
                .replace("%ip%", serverPlayer.getInetAddress().getHostAddress()));

        Keyboard keyboard = Keyboard.builder()
                .buttons(List.of(List.of(
                        Button.builder()
                                .type(ButtonType.SUCCESS)
                                .label(getSocialConfig().getMessages().getConfirmationButtonAcceptText())
                                .callbackData("confirm-accept:" + player.getLowercaseName())
                                .build(),
                        Button.builder()
                                .type(ButtonType.DANGER)
                                .label(getSocialConfig().getMessages().getConfirmationButtonDeclineText())
                                .callbackData("confirm-decline:" + player.getLowercaseName())
                                .build()
                )))
                .build();
        Object objectKeyboard = createKeyboard(keyboard);
        if (objectKeyboard instanceof InlineKeyboardMarkup markup) {
            sendMessage.replyMarkup(markup);
        }

        if (execute(sendMessage) == 403) {
            serverPlayer.disconnect(plugin.getConfig().getMessages().getTelegramPrivateMessagesClosed());
            return false;
        }
        return true;
    }

    @Override
    public List<ProtectedPlayer> getLinkedPlayersById(Object id) {
        if (!(id instanceof Long))
            return Collections.emptyList();

        try {
            ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
            return dao.queryForEq("telegram", id);
        } catch (SQLException e) {
            logger.error("TelegramSocial#getLinkedPlayersById have SQLException: " + e.getMessage());
        }
        return null;
    }

    @Override
    public TelegramConfig getSocialConfig() {
        return plugin.getTelegramConfig();
    }


    public int execute(BaseRequest<?, ?> request) {
        BaseResponse response = telegramBot.execute(request);
        if (!response.isOk()) {
            plugin.getServerLogger().warn("Error on request to Telegram. (%d): %s".formatted(response.errorCode(), response.description()));
            return response.errorCode();
        }
        return -1;
    }
}
