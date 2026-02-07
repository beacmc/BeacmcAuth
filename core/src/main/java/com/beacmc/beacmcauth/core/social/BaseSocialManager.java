package com.beacmc.beacmcauth.core.social;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.Social;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.social.command.SocialCommand;
import com.beacmc.beacmcauth.api.social.command.SocialCommandRegistry;
import com.beacmc.beacmcauth.api.social.confirmation.ConfirmationPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickListener;
import com.beacmc.beacmcauth.api.social.keyboard.button.listener.ButtonClickRegistry;
import com.beacmc.beacmcauth.api.social.link.LinkManager;
import com.beacmc.beacmcauth.core.social.base.button.AccountPageClick;
import com.beacmc.beacmcauth.core.social.base.button.AccountSettingClick;
import com.beacmc.beacmcauth.core.social.base.button.BaseButtonClickRegistry;
import com.beacmc.beacmcauth.core.social.base.button.ConfirmationButtonClick;
import com.beacmc.beacmcauth.core.social.base.command.AccountListCommand;
import com.beacmc.beacmcauth.core.social.base.command.BaseSocialCommandRegistry;
import com.beacmc.beacmcauth.core.social.base.command.LinkCommand;
import com.beacmc.beacmcauth.core.social.base.command.StartCommand;
import com.beacmc.beacmcauth.core.social.link.BaseLinkManager;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ToString
public class BaseSocialManager implements SocialManager {

    private final List<ConfirmationPlayer> confirmationPlayers;
    private final List<Social<?, ?>> socials;
    private final BeacmcAuth plugin;
    private final SocialCommandRegistry socialCommandRegistry;
    private final ButtonClickRegistry buttonClickRegistry;
    private final LinkManager linkManager;

    public BaseSocialManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.confirmationPlayers = new ArrayList<>();
        this.socials = new ArrayList<>();
        this.socialCommandRegistry = new BaseSocialCommandRegistry();
        this.buttonClickRegistry = new BaseButtonClickRegistry();
        this.linkManager = new BaseLinkManager();
        List<SocialCommand> commandToRegister = List.of(
                new AccountListCommand(plugin),
                new LinkCommand(plugin),
                new StartCommand()
        );

        List<ButtonClickListener> listenerToRegister = List.of(
                new AccountPageClick(plugin),
                new AccountSettingClick(plugin),
                new ConfirmationButtonClick(plugin)
        );

        buttonClickRegistry.getListeners().addAll(listenerToRegister);
        socialCommandRegistry.getCommands().addAll(commandToRegister);
    }

    @Override
    public @NotNull List<ConfirmationPlayer> getConfirmationPlayers() {
        return confirmationPlayers;
    }

    @Override
    public @NotNull List<Social<?, ?>> getSocials() {
        return socials;
    }

    @Override
    public boolean startPlayerConfirmations(ProtectedPlayer player) {
        final ServerLogger logger = plugin.getServerLogger();
        final List<Social<?, ?>> linkedSocials = getPlayerLinkedSocials(player).stream()
                .filter(social -> {
                    boolean isInit = social.isInit();
                    boolean isPlayerTwoFaEnabled = social.isPlayerTwoFaEnabled(player);
                    logger.debug("Check social(%s). Init: %b".formatted(social.getClass().getName(), isInit));
                    logger.debug("Check social(%s). Player(%s) 2FA enabled: %b".formatted(social.getClass().getName(), player.getLowercaseName(), isPlayerTwoFaEnabled));
                    return isInit && isPlayerTwoFaEnabled;
                })
                .collect(Collectors.toList());

        logger.debug("The player(" + player.getRealName() + ") started the login confirmation: " + linkedSocials);

        if (!linkedSocials.isEmpty()) {
            ConfirmationPlayer confirmationPlayerTemplate = getConfirmationByPlayer(player);
            ConfirmationPlayer confirmationPlayer = confirmationPlayerTemplate == null
                    ? new ConfirmationPlayer(player, linkedSocials, null)
                    : confirmationPlayerTemplate;

            getConfirmationPlayers().add(confirmationPlayer);
            return confirmationPlayer.nextConfirmationSocial(plugin);
        }
        return false;
    }

    @Override
    public @NotNull List<Social<?, ?>> getPlayerLinkedSocials(ProtectedPlayer player) {
        return socials.stream()
                .filter(social -> social.isPlayerLinked(player))
                .collect(Collectors.toList());
    }

    @Override
    public ButtonClickRegistry getButtonClickRegistry() {
        return buttonClickRegistry;
    }

    @Override
    public SocialCommandRegistry getSocialCommandRegistry() {
        return socialCommandRegistry;
    }

    @Override
    public LinkManager getLinkManager() {
        return linkManager;
    }
}
