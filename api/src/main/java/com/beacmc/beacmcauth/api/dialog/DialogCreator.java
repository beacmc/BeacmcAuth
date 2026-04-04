package com.beacmc.beacmcauth.api.dialog;

import com.beacmc.beacmcauth.api.PlaceholderSupport;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface DialogCreator extends PlaceholderSupport {

    Dialog createDialog(DialogManager manager, @Nullable Map<String, ?> placeholders);

    default Dialog createDialog(DialogManager manager) {
        return createDialog(manager, null);
    }
}
