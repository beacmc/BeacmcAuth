package com.beacmc.beacmcauth.core.social.types.vkontakte;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import com.beacmc.beacmcauth.core.cache.cooldown.VkontakteCooldown;
import com.beacmc.beacmcauth.core.util.runnable.VkontakteRunnable;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.KeyboardButton;
import com.vk.api.sdk.objects.messages.KeyboardButtonAction;
import com.vk.api.sdk.objects.messages.KeyboardButtonColor;
import com.vk.api.sdk.objects.messages.TemplateActionTypeNames;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import lombok.ToString;

import java.sql.SQLException;
import java.util.*;

@ToString
public class VkontakteSocial implements Social<VkApiClient, Integer> {

    private final VkApiClient client;
    private final BeacmcAuth plugin;
    private final VkontakteCooldown cooldown;
    private final GroupActor groupActor;
    private final Random random;
    private final ServerLogger logger;

    public VkontakteSocial(BeacmcAuth plugin, VkApiClient client) {
        this.client = client;
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
        this.cooldown = VkontakteCooldown.getInstance();
        this.groupActor = plugin.getVkApiPlugin().getVkApiProvider().getActor();
        this.random = new Random();
        try {
            client.groups()
                    .setSettings(plugin.getVkApiPlugin().getVkApiProvider().getActor(), plugin.getVkApiPlugin().getVkApiProvider().getActor().getGroupId())
                    .botsCapabilities(true)
                    .botsStartButton(true)
                    .messages(true)
                    .execute();

            client.groups()
                    .setLongPollSettings(plugin.getVkApiPlugin().getVkApiProvider().getActor(), plugin.getVkApiPlugin().getVkApiProvider().getActor().getGroupId())
                    .apiVersion("5.131")
                    .messageNew(true)
                    .messageEvent(true)
                    .enabled(true)
                    .execute();

        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isEnabled() {
        return getSocialConfig().isEnabled();
    }

    @Override
    public boolean isInit() {
        return isEnabled();
    }

    @Override
    public Object createKeyboard(Keyboard keyboard) {
        com.vk.api.sdk.objects.messages.Keyboard vkKeyboard = new com.vk.api.sdk.objects.messages.Keyboard();
        vkKeyboard.setInline(true);

        List<List<KeyboardButton>> vkKeyboardLines = new ArrayList<>();
        keyboard.getButtons().forEach(buttons -> {
            List<KeyboardButton> vkButtonLine = new ArrayList<>();
            buttons.forEach(button -> {
                vkButtonLine.add(new KeyboardButton()
                        .setAction(new KeyboardButtonAction()
                                .setLabel(button.getLabel())
                                .setType(TemplateActionTypeNames.CALLBACK)
                                .setPayload("\"%s\"".formatted(button.getCallbackData()))
                        )
                        .setColor(getButtonColor(button.getType())));
            });
            vkKeyboardLines.add(vkButtonLine);
        });
        return vkKeyboard.setButtons(vkKeyboardLines);
    }

    @Override
    public VkApiClient getOriginalSocial() {
        return client;
    }

    @Override
    public SocialType getType() {
        return SocialType.VKONTAKTE;
    }

    @Override
    public boolean isPlayerTwoFaEnabled(ProtectedPlayer player) {
        return isPlayerLinked(player) && player.isVkontakteTwoFaEnabled();
    }

    @Override
    public void switchPlayerTwoFa(ProtectedPlayer player, boolean enabled) {
        try {
            plugin.getDatabase().getProtectedPlayerDao().createOrUpdate(player.setVkontakteTwoFaEnabled(enabled));
            plugin.getAuthManager().getPlayerCache().addOrUpdateCache(player);
        } catch (SQLException e) {
            logger.error("VkontakteSocial#switchPlayerTwoFa have SQLException: " + e.getMessage());
        }
    }

    @Override
    public boolean isPlayerLinked(ProtectedPlayer player) {
        return player.getVkontakte() != 0;
    }

    @Override
    public void linkPlayer(ProtectedPlayer player, Object id) {
        if (player.getVkontakte() != 0 || !(id instanceof Integer longId)) return;

        try {
            player.setVkontakte(longId);
            plugin.getDatabase().getProtectedPlayerDao().createOrUpdate(player);
            plugin.getAuthManager().getPlayerCache().addOrUpdateCache(player);
        } catch (SQLException e) {
            logger.error("VkontakteSocial#linkPlayer have SQLException: " + e.getMessage());
        }
    }

    @Override
    public void unlinkPlayer(ProtectedPlayer player) {
        if (player.getVkontakte() == 0) return;

        try {
            player.setVkontakte(0);
            plugin.getDatabase().getProtectedPlayerDao().createOrUpdate(player);
            plugin.getAuthManager().getPlayerCache().addOrUpdateCache(player);
        } catch (SQLException e) {
            logger.error("VkontakteSocial#unlinkPlayer have SQLException: " + e.getMessage());
        }
    }

    @Override
    public String getLinkCommandPrefix() {
        return getSocialConfig().getLinkCommand();
    }

    @Override
    public SocialConfig getSocialConfig() {
        return plugin.getVkontakteConfig();
    }

    @Override
    public AbstractCooldown<Integer> getCooldownCache() {
        return VkontakteCooldown.getInstance();
    }

    @Override
    public boolean isCooldown(Object id) {
        if (!(id instanceof Integer longId))
            return false;

        return cooldown.isCooldown(longId);
    }

    @Override
    public void createCooldown(Object id, long time) {
        if (!(id instanceof Integer longId))
            return;

        cooldown.createCooldown(longId, time);
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

        if (confirmationPlayer == null || player.getVkontakte() == 0) {
            serverPlayer.disconnect(plugin.getConfig().getMessage("internal-error"));
            return false;
        }

        new VkontakteRunnable(plugin, serverPlayer, player);

        try {
            MessagesSendQuery query = client.messages().send(groupActor).peerId(player.getVkontakte());
            query.message(getSocialConfig().getMessage("confirmation-message", Map.of("%name%", player.getLowercaseName(), "%ip%", serverPlayer.getInetAddress().getHostAddress())));
            Keyboard keyboard = Keyboard.builder()
                    .buttons(List.of(List.of(
                            Button.builder()
                                    .type(ButtonType.SUCCESS)
                                    .label(getSocialConfig().getMessage("confirmation-button-accept-text"))
                                    .callbackData("confirm-accept:" + player.getLowercaseName())
                                    .build(),
                            Button.builder()
                                    .type(ButtonType.DANGER)
                                    .label(getSocialConfig().getMessage("confirmation-button-decline-text"))
                                    .callbackData("confirm-decline:" + player.getLowercaseName())
                                    .build()
                    )))
                    .build();
            Object objectKeyboard = createKeyboard(keyboard);
            if (objectKeyboard instanceof com.vk.api.sdk.objects.messages.Keyboard markup) {
                query.keyboard(markup);
            }
            query.randomId(random.nextInt()).execute();
        } catch (ApiException e) {
            if (e.getCode() == 901) {
                serverPlayer.disconnect(plugin.getConfig().getMessage("vkontakte-private-messages-closed"));
            } else {
                e.printStackTrace();
            }
        } catch (ClientException e) {
            serverPlayer.disconnect(plugin.getConfig().getMessage("internal-error"));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String getGameConfigPrefixMessage() {
        return "vkontakte-";
    }

    @Override
    public List<ProtectedPlayer> getLinkedPlayersById(Object id) {
        try {
            return plugin.getDatabase().getProtectedPlayerDao().queryForEq("vkontakte", id);
        } catch (SQLException e) {
            logger.error("VkontakteSocial#getLinkedPlayersById have SQLException: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private KeyboardButtonColor getButtonColor(ButtonType type) {
        return switch (type) {
            case PRIMARY -> KeyboardButtonColor.PRIMARY;
            case DANGER -> KeyboardButtonColor.NEGATIVE;
            case SUCCESS -> KeyboardButtonColor.POSITIVE;
            default -> KeyboardButtonColor.DEFAULT;
        };
    }
}
