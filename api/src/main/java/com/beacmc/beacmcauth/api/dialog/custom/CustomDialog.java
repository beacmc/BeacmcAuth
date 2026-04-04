package com.beacmc.beacmcauth.api.dialog.custom;

import com.beacmc.beacmcauth.api.cache.CachedData;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class CustomDialog implements CachedData<DialogUniqueId> {

    private DialogUniqueId uniqueId;
    private Dialog dialog;

    @Override
    public DialogUniqueId getId() {
        return uniqueId;
    }
}
