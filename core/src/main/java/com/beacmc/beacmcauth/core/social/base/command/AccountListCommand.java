package com.beacmc.beacmcauth.core.social.base.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.command.SocialCommand;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class AccountListCommand implements SocialCommand {

    private final BeacmcAuth plugin;

    @Override
    public void execute(SocialPlayer<?, ?> socialPlayer, Social<?, ?> social, String prefix, String[] args) {
        if (!social.getSocialConfig().getAccountsCommand().equals(prefix)) return;

        SocialConfig socialConfig = social.getSocialConfig();

        if (social.isCooldown(socialPlayer.getID())) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getCooldown());
            return;
        }
        social.createCooldown(socialPlayer.getID(), 500L);

        List<ProtectedPlayer> accountLinked = social.getLinkedPlayersById(socialPlayer.getID());
        if (accountLinked.isEmpty()) {
            socialPlayer.sendPrivateMessage(socialConfig.getMessages().getAccountsEmpty());
            return;
        }

        int end = Math.min(4, accountLinked.size());
        List<ProtectedPlayer> pageContent = accountLinked.subList(0, end);

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
                .callbackData("next:0")
                .build();

        if (end < accountLinked.size())
            buttons.add(List.of(nextPageButton));

        socialPlayer.sendPrivateMessage(socialConfig.getMessages().getChooseAccount(), social.createKeyboard(new Keyboard(buttons)));
    }
}
