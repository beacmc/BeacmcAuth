package com.beacmc.beacmcauth.core.packet.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.dialog.DialogClickListener;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialogListener implements PacketListener {

    private final BeacmcAuth plugin;

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CUSTOM_CLICK_ACTION) {
            final DialogManager dialogManager = plugin.getDialogManager();

            WrapperCommonClientCustomClickAction<?> wrapper = new WrapperCommonClientCustomClickAction<>(event);
            User user = event.getUser();

            if (user == null) return;

            for (DialogClickListener listener : dialogManager.getDialogClickListeners()) {
                listener.onClick(user, wrapper);
            }
        }
    }
}
