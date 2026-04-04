package com.beacmc.beacmcauth.core.dialog.listener;

import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.AuthenticatingPlayer;
import com.beacmc.beacmcauth.api.dialog.DialogClickListener;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.beacmc.beacmcauth.api.dialog.custom.DialogType;
import com.beacmc.beacmcauth.api.server.Proxy;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@RequiredArgsConstructor
public class ChooseAuthDialogListener implements DialogClickListener {

    private final AuthManager authManager;
    private final DialogManager dialogManager;
    private final Proxy proxy;

    @Override
    public void onClick(User user, WrapperCommonClientCustomClickAction<?> clickAction) {
        if (clickAction.getId().toString().equals("beacmcauth:open-auth-dialog")
                && authManager.isAuthenticating(user.getName().toLowerCase())) {

            ServerPlayer player = proxy.getPlayer(user.getUUID());
            AuthenticatingPlayer authPlayer = authManager.getAuthPlayers()
                    .getCacheData(user.getName().toLowerCase());

            if (authPlayer.getPlayer().isRegister()) {
                Dialog dialog = dialogManager.getDialog(DialogType.LOGIN);
                player.showDialog(dialog);
            } else {
                Dialog dialog = dialogManager.getDialog(DialogType.REGISTER);
                player.showDialog(dialog);
            }
        }
    }
}
