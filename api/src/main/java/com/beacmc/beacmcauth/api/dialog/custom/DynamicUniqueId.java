package com.beacmc.beacmcauth.api.dialog.custom;

import org.jetbrains.annotations.NotNull;

public record DynamicUniqueId(@NotNull String id) implements DialogUniqueId {
}
