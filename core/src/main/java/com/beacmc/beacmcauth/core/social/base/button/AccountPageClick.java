package com.beacmc.beacmcauth.core.social.base.button;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickListener;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class AccountPageClick implements ButtonClickListener {

    private final BeacmcAuth plugin;

    @Override
    public void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, Button button) {
        final SocialConfig socialConfig = social.getSocialConfig();

        if (button.getCallbackData().startsWith("previous") || button.getCallbackData().startsWith("next")) {
            String[] args = button.getCallbackData().split(":");
            if (args.length < 2) return;

            if (social.isCooldown(socialPlayer.getID())) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getCooldown());
                return;
            }
            social.createCooldown(socialPlayer.getID(), 6000L);

            final List<ProtectedPlayer> accountLinked = social.getLinkedPlayersById(socialPlayer.getID());
            final int currentPage = Integer.parseInt(args[1]);
            final int page = button.getCallbackData().startsWith("previous") ? currentPage - 1 : currentPage + 1;
            final int start = page * 4;
            final int end = Math.min(4, accountLinked.size());
            final List<ProtectedPlayer> pageContent = accountLinked.subList(start, end);

            List<List<Button>> buttons = new ArrayList<>();
            pageContent.forEach(account -> buttons.add(List.of(
                    Button.builder()
                            .label(account.getRealName())
                            .type(ButtonType.SECONDARY)
                            .callbackData("account:%s".formatted(account.getLowercaseName()))
                            .build()
            )));
            Button nextPageButton = Button.builder()
                    .type(ButtonType.PRIMARY)
                    .label("⏩")
                    .callbackData("next:%d".formatted(page))
                    .build();
            Button previousPageButton = Button.builder()
                    .label("⏪")
                    .callbackData("previous:%d".formatted(page))
                    .type(ButtonType.PRIMARY)
                    .build();

            List<Button> pageButtons = new ArrayList<>();

            if (page > 0) pageButtons.add(previousPageButton);
            if (end < accountLinked.size()) pageButtons.add(nextPageButton);
            if (!pageButtons.isEmpty()) buttons.add(pageButtons);

            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getChooseAccount(), social.createKeyboard(new Keyboard(buttons)));
        }

        if (button.getCallbackData().startsWith("account")) {
            String[] args = button.getCallbackData().split(":");
            if (args.length < 2) return;

            if (social.isCooldown(socialPlayer.getID())) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getCooldown());
                return;
            }
            social.createCooldown(socialPlayer.getID(), 6000L);

            plugin.getAuthManager().getProtectedPlayer(args[1]).thenAccept(player -> {
                if (!socialPlayer.checkAccountLink(player)) {
                    socialPlayer.sendPrivateMessage(socialConfig.getMessages().getAccountNotLinked());
                    return;
                }

                String playerOnline = socialConfig.getMessages().getPlayerInfoOnline();
                String playerOffline = socialConfig.getMessages().getPlayerOffline();
                String message = socialConfig.getMessages().getAccountInfo()
                        .replace("%name%", player.getRealName())
                        .replace("%lowercase_name%", player.getLowercaseName())
                        .replace("%last_ip%", player.getLastIp())
                        .replace("%reg_ip%", player.getRegisterIp())
                        .replace("%is_online%", plugin.getProxy().getPlayer(player.getUuid()) == null ? playerOffline : playerOnline);


                String id = player.getLowercaseName();

                List<List<Button>> buttons = new ArrayList<>();
                buttons.add(List.of(
                        Button.builder()
                                .label(socialConfig.getMessages().getAccount2faToggleButton())
                                .callbackData("toggle-2fa:" + id)
                                .type(ButtonType.SECONDARY)
                                .build(),
                        Button.builder()
                                .label(socialConfig.getMessages().getAccountResetPasswordButton())
                                .callbackData("reset-password:" + id)
                                .type(ButtonType.SECONDARY)
                                .build()
                ));
                buttons.add(List.of(
                        Button.builder()
                                .label(socialConfig.getMessages().getAccountBanToggleButton())
                                .callbackData("toggle-ban:" + id)
                                .type(ButtonType.SECONDARY)
                                .build(),
                        Button.builder()
                                .label(socialConfig.getMessages().getAccountKickButton())
                                .callbackData("kick:" + id)
                                .type(ButtonType.SECONDARY)
                                .build()
                ));

                if (!socialConfig.isDisableUnlink()) {
                    buttons.add(List.of(
                            Button.builder()
                                    .label(socialConfig.getMessages().getAccountUnlinkButton())
                                    .callbackData("unlink:" + id)
                                    .type(ButtonType.DANGER)
                                    .build()
                    ));
                }

                socialPlayer.sendPrivateMessage(message, social.createKeyboard(new Keyboard(buttons)));
            });
        }
    }
}
