package com.beacmc.beacmcauth.api.dialog;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;

public interface DialogClickListener {

    void onClick(User user, WrapperCommonClientCustomClickAction<?> clickAction);
}
