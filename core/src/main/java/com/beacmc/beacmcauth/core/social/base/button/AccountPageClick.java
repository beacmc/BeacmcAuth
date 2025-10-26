package com.beacmc.beacmcauth.core.social.base.button;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickListener;
import lombok.AllArgsConstructor;

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

            Keyboard keyboard = socialConfig.getKeyboards().createAccountsListKeyboard(accountLinked, page);
            if (keyboard != null) {
                socialPlayer.sendPrivateMessage(socialConfig.getMessages().getChooseAccount(), social.createKeyboard(keyboard));
            }
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

                socialPlayer.sendPrivateMessage(message, social.createKeyboard(socialConfig.getKeyboards().createAccountManageKeyboard(player)));
            });
        }
    }
}
