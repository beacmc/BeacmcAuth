package com.beacmc.beacmcauth.core.dialog;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.dialog.*;
import com.beacmc.beacmcauth.api.dialog.custom.CustomDialog;
import com.beacmc.beacmcauth.api.dialog.custom.DialogType;
import com.beacmc.beacmcauth.api.dialog.custom.DialogUniqueId;
import com.beacmc.beacmcauth.api.dialog.custom.DynamicUniqueId;
import com.beacmc.beacmcauth.core.cache.CustomDialogCache;
import com.beacmc.beacmcauth.core.config.dialog.ChooseDialogSettings;
import com.beacmc.beacmcauth.core.config.dialog.LoginDialogSettings;
import com.beacmc.beacmcauth.core.config.dialog.RegisterDialogSettings;
import com.beacmc.beacmcauth.core.dialog.listener.*;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class BaseDialogManager implements DialogManager {

    private final BeacmcAuth plugin;
    private final Cache<CustomDialog, DialogUniqueId> dialogs = new CustomDialogCache();
    private final List<DialogClickListener> dialogClickListeners = new ArrayList<>();

    public void loadDefaultDialogs() {
        final Config config = plugin.getConfig();

        ChooseDialogSettings chooseDialogSettings = (ChooseDialogSettings) config.getChooseDialogSettings();
        this.register(
                DialogType.CHOOSE_DIALOG_FULL,
                chooseDialogSettings.createDialog(this)
        );
        this.register(
                DialogType.CHOOSE_DIALOG_WITHOUT_EMAIL,
                chooseDialogSettings.createDialog(this, false, true)
        );
        this.register(
                DialogType.CHOOSE_DIALOG_WITHOUT_SECRET_QUESTION,
                chooseDialogSettings.createDialog(this, true, false)
        );

        RegisterDialogSettings registerDialogSettings = (RegisterDialogSettings) config.getRegisterDialogSettings();
        this.register(
                DialogType.REGISTER,
                registerDialogSettings.createDialog(
                        this,
                        Map.of("%error%", ""),
                        config.isRegisterRepeatPassword()
                )
        );

        LoginDialogSettings loginDialogSettings = (LoginDialogSettings) config.getLoginDialogSettings();
        this.register(
                DialogType.LOGIN,
                loginDialogSettings.createDialog(this, Map.of("%error%", ""))
        );
    }

    public void registerDefaultListeners() {
        registerListener(new LeaveListener(plugin));
        registerListener(new LoginListener(plugin));
        registerListener(new RegisterListener(plugin));
        registerListener(new SecretListener(plugin));
        registerListener(new ChooseAuthDialogListener(
                plugin.getAuthManager(),
                plugin.getDialogManager(),
                plugin.getProxy()
        ));
    }

    @Override
    public Dialog buildAndSave(@NotNull String dialogKey,
                               @NotNull Function<DialogManager, Dialog> function,
                               @NotNull Map<String, ?> placeholders) {

        DialogUniqueId cacheId = new DynamicUniqueId(dialogKey + placeholders.entrySet().stream()
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.joining(",")));

        CustomDialog cachedDialog = dialogs.getCacheData(cacheId);
        if (cachedDialog != null) {
            return cachedDialog.getDialog();
        }

        Dialog dialog = function.apply(this);
        if (dialog != null) {
            register(cacheId, dialog);
        }
        return dialog;
    }
}
