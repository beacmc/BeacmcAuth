package com.beacmc.beacmcauth.core.config.dialog;

import com.beacmc.beacmcauth.api.AdventureColor;
import com.beacmc.beacmcauth.api.config.dialog.button.ClickEventType;
import com.beacmc.beacmcauth.api.dialog.DialogCreator;
import com.beacmc.beacmcauth.api.config.dialog.button.DialogButton;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.beacmc.beacmcauth.api.dialog.custom.DialogType;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import de.exlll.configlib.Configuration;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public final class ChooseDialogSettings implements DialogCreator {

    private String title = "Choose action";
    private String externalTitle = null;
    private String body = """
            
            """;
    private int bodyWidth = 200;
    private int columns = 2;
    private DialogButton authButton = DialogButton.builder()
            .label("Default Authorization")
            .tooltip("Click")
            .width(200)
            .build();
    private DialogButton emailRecoveryButton = DialogButton.builder()
            .label("Email Recovery")
            .tooltip("Click")
            .width(200)
            .build();
    private DialogButton secretQuestionRecoveryButton = DialogButton.builder()
            .label("Secret Question Recovery")
            .tooltip("Click")
            .width(200)
            .build();

    private DialogButton leaveButton = DialogButton.builder()
            .label("Leave the server")
            .tooltip("Click")
            .width(200)
            .build();

    @Override
    public Dialog createDialog(DialogManager manager, Map<String, ?> ignored) {
        return createDialog(manager, true, true);
    }

    public Dialog createDialog(DialogManager manager, boolean email, boolean secretQuestion) {
        CommonDialogData commonDialogData = new CommonDialogData(
                AdventureColor.of(title),
                AdventureColor.of(externalTitle),
                false,
                false,
                DialogAction.NONE,
                List.of(new PlainMessageDialogBody(new PlainMessage(AdventureColor.of(body), bodyWidth))),
                List.of()
        );
        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(authButton.setClickType(ClickEventType.DYNAMIC_ACTION)
                .setActionValue("open-auth-dialog")
                .createActionButton(manager));

        if (email) {
            buttons.add(emailRecoveryButton.setClickType(ClickEventType.PLAYER_COMMAND)
                    .setCommand("/email recovery")
                    .createActionButton(manager));
        }

        if (secretQuestion) {
            buttons.add(secretQuestionRecoveryButton.setClickType(ClickEventType.DYNAMIC_ACTION)
                    .setActionValue("secret-recovery")
                    .createActionButton(manager));
        }

        return new MultiActionDialog(
                commonDialogData,
                buttons,
                leaveButton.setClickType(ClickEventType.DYNAMIC_ACTION)
                        .setActionValue("leave")
                        .createActionButton(manager),
                columns
        );
    }
}
