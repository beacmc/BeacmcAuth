package com.beacmc.beacmcauth.api.dialog.custom;

import org.jetbrains.annotations.NotNull;

public enum DialogType implements DialogUniqueId {

    CHOOSE_DIALOG_FULL("choose-dialog"),
    CHOOSE_DIALOG_WITHOUT_EMAIL("choose-dialog-without-email"),
    CHOOSE_DIALOG_WITHOUT_SECRET_QUESTION("choose-dialog-without-secret-question"),
    REGISTER("register"),
    LOGIN("login");

    private final String id;

    DialogType(@NotNull String id) {
        this.id = id;
    }

    public @NotNull String id() {
        return id;
    }
}
