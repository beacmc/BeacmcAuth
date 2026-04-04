package com.beacmc.beacmcauth.core.dialog.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.AuthenticatingPlayer;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.dialog.DialogClickListener;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.core.cache.cooldown.GameCooldown;
import com.beacmc.beacmcauth.core.config.dialog.RegisterDialogSettings;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class RegisterListener implements DialogClickListener {

    private final BeacmcAuth plugin;
    private final GameCooldown cooldown = GameCooldown.getInstance();

    @Override
    public void onClick(User user, WrapperCommonClientCustomClickAction<?> clickAction) {
        if (clickAction.getId().toString().equals("beacmcauth:confirm-register")
                && clickAction.getPayload() instanceof NBTCompound nbt) {

            final AuthManager authManager = plugin.getAuthManager();
            final Cache<AuthenticatingPlayer, String> authCache = authManager.getAuthPlayers();
            final Config config = plugin.getConfig();

            ServerPlayer player = plugin.getProxy().getPlayer(user.getUUID());
            AuthenticatingPlayer authPlayer = authCache.getCacheData(user.getName().toLowerCase());
            ProtectedPlayer protectedPlayer = authPlayer.getPlayer();

            boolean repeatPassword = config.isRegisterRepeatPassword();
            String firstPassword = nbt.getStringTagValueOrDefault("password", "");
            String secondPassword = nbt.getStringTagValueOrDefault("repeatPassword", "");

            if (cooldown.isCooldown(player.getLowercaseName())) {
                Dialog dialog = buildDialogWithError(config.getMessages().getDialogCooldown(), repeatPassword);
                player.showDialog(dialog);
                return;
            }
            cooldown.createCooldown(player.getLowercaseName(), 1_000);

            if (repeatPassword && secondPassword.isEmpty()) {
                player.showDialog(buildDialogWithError(
                        config.getMessages().getDialogConfirmPassword(),
                        true
                ));
                return;
            }

            if (repeatPassword && !firstPassword.equals(secondPassword)) {
                player.showDialog(buildDialogWithError(
                        config.getMessages().getDialogPasswordsDontMatch(),
                        true
                ));
                return;
            }

            if (firstPassword.isEmpty() || !config.getPasswordRegex().matcher(firstPassword).matches()) {
                player.showDialog(buildDialogWithError(
                        config.getMessages().getDialogInvalidPassword(),
                        repeatPassword
                ));
                return;
            }

            player.sendMessage(config.getMessages().getRegisterSuccess()
                    .replace("%password%", firstPassword));

            player.closeDialog();

            player.sendTitle("&7", "&7", 0, 25, 0);
            plugin.getSongManager().stop(player.getUUID());
            authManager.getAuthPlayers().removeById(protectedPlayer.getLowercaseName());
            authManager.register(protectedPlayer, firstPassword);
            authManager.connectPlayer(player, config.findServer(config.getLobbyServers()));
        }
    }

    private Dialog buildDialogWithError(String error, boolean repeatPassword) {
        Map<String, ?> placeholders = Map.of("%error%", error);
        RegisterDialogSettings registerDialogSettings = (RegisterDialogSettings) plugin.getConfig().getRegisterDialogSettings();
        return plugin.getDialogManager().buildAndSave(
                getClass().getSimpleName(),
                (manager) -> registerDialogSettings.createDialog(
                        manager,
                        placeholders,
                        repeatPassword
                ),
                placeholders
        );
    }
}
