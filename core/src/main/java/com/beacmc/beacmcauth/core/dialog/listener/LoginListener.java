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
import com.beacmc.beacmcauth.core.config.dialog.LoginDialogSettings;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.Map;

@RequiredArgsConstructor
public class LoginListener implements DialogClickListener {

    private final BeacmcAuth plugin;
    private final GameCooldown cooldown = GameCooldown.getInstance();

    @Override
    public void onClick(User user, WrapperCommonClientCustomClickAction<?> clickAction) {
        if (clickAction.getId().toString().equals("beacmcauth:confirm-login")
                && clickAction.getPayload() instanceof NBTCompound nbt) {

            final AuthManager authManager = plugin.getAuthManager();
            final Cache<AuthenticatingPlayer, String> authCache = authManager.getAuthPlayers();
            final Config config = plugin.getConfig();

            ServerPlayer player = plugin.getProxy().getPlayer(user.getUUID());
            AuthenticatingPlayer authPlayer = authCache.getCacheData(user.getName().toLowerCase());

            if (!authCache.contains(user.getName().toLowerCase())) {
                player.closeDialog();
                return;
            }

            if (cooldown.isCooldown(player.getLowercaseName())) {
                Dialog dialog = buildDialogWithError(config.getMessages().getDialogCooldown());
                player.showDialog(dialog);
                return;
            }
            cooldown.createCooldown(player.getLowercaseName(), 1_000);

            ProtectedPlayer protectedPlayer = authPlayer.getPlayer();
            String password = nbt.getStringTagValueOrNull("password");

            if (password == null
                    || password.isEmpty()
                    || !protectedPlayer.checkPassword(password)) {
                if (!authPlayer.useAttempt()) {
                    player.disconnect(config.getMessages().getAttemptsLeft());
                } else {
                    Dialog dialog = buildDialogWithError(
                            config.getMessages().getDialogWrongPassword()
                                    .replace("%attempts%", String.valueOf(authPlayer.getAttempts()))
                    );
                    player.showDialog(dialog);
                }
                return;
            }

            player.sendMessage(config.getMessages().getLoginSuccess());
            player.sendTitle("&7", "&7", 0, 25, 0);
            authManager.getAuthPlayers().removeById(protectedPlayer.getLowercaseName());
            player.closeDialog();

            if (!plugin.getSocialManager().startPlayerConfirmations(protectedPlayer)) {
                authManager.performLogin(protectedPlayer);
                plugin.getSongManager().stop(player.getUUID());
                authManager.connectPlayer(player, config.findServer(config.getLobbyServers()));
            }
        }
    }

    private Dialog buildDialogWithError(String error) {
        Map<String, ?> placeholders = Map.of("%error%", error);
        LoginDialogSettings loginDialogSettings = (LoginDialogSettings) plugin.getConfig().getLoginDialogSettings();
        return plugin.getDialogManager().buildAndSave(
                getClass().getSimpleName(),
                (manager) -> loginDialogSettings.createDialog(manager, placeholders),
                placeholders
        );
    }
}
