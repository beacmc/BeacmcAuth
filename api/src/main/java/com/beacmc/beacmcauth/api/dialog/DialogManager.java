package com.beacmc.beacmcauth.api.dialog;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.dialog.custom.CustomDialog;
import com.beacmc.beacmcauth.api.dialog.custom.DialogType;
import com.beacmc.beacmcauth.api.dialog.custom.DialogUniqueId;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface DialogManager {

    default void registerListener(DialogClickListener listener) {
        getDialogClickListeners().add(listener);
    }

    default void register(DialogUniqueId uniqueId, Dialog dialog) {
        getDialogs().addCache(new CustomDialog(uniqueId, dialog));
    }

    default void unregister(DialogUniqueId uniqueId) {
        getDialogs().removeById(uniqueId);
    }

    default @Nullable Dialog getDialog(@NotNull DialogUniqueId uniqueId) {
        return getDialog(uniqueId.id());
    }

    default @Nullable Dialog getDialog(@NotNull String uniqueId) {
        CustomDialog customDialog = getCustomDialog(uniqueId);
        return customDialog != null ? customDialog.getDialog() : null;
    }

    default @Nullable CustomDialog getCustomDialog(@NotNull DialogUniqueId uniqueId) {
        return getCustomDialog(uniqueId.id());
    }

    default @Nullable CustomDialog getCustomDialog(@NotNull String uniqueId) {
        return getDialogs().stream()
                .filter(cd -> cd.getUniqueId().id().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    Cache<CustomDialog, DialogUniqueId> getDialogs();

    List<DialogClickListener> getDialogClickListeners();

    Dialog buildAndSave(@NotNull String dialogKey, @NotNull Function<DialogManager, Dialog> function, @NotNull Map<String, ?> placeholders);
}
