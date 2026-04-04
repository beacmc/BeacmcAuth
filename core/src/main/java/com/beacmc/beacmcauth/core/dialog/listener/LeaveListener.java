package com.beacmc.beacmcauth.core.dialog.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.dialog.DialogClickListener;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LeaveListener implements DialogClickListener {

    private final BeacmcAuth plugin;

    @Override
    public void onClick(User user, WrapperCommonClientCustomClickAction<?> clickAction) {
        if (clickAction.getId().toString().equals("beacmcauth:leave")) {
            ServerPlayer player = plugin.getProxy().getPlayer(user.getUUID());
            if (player != null) {
                player.closeDialog();
            }
        }
    }
}
