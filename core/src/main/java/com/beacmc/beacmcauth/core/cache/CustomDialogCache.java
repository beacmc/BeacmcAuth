package com.beacmc.beacmcauth.core.cache;

import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.dialog.custom.CustomDialog;
import com.beacmc.beacmcauth.api.dialog.custom.DialogUniqueId;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ToString
public class CustomDialogCache implements Cache<CustomDialog, DialogUniqueId> {

    private final Map<DialogUniqueId, CustomDialog> caches = new ConcurrentHashMap<>();
}
