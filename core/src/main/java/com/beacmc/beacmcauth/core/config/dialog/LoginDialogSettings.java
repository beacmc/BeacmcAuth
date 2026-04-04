package com.beacmc.beacmcauth.core.config.dialog;

import com.beacmc.beacmcauth.api.AdventureColor;
import com.beacmc.beacmcauth.api.config.dialog.button.ClickEventType;
import com.beacmc.beacmcauth.api.config.dialog.button.DialogButton;
import com.beacmc.beacmcauth.api.config.dialog.input.DialogTextInput;
import com.beacmc.beacmcauth.api.dialog.DialogCreator;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.NoticeDialog;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@ToString
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class LoginDialogSettings implements DialogCreator {

    private String title = "Login";
    private String externalTitle = null;
    private String body = """
            %error%
            """;
    private int bodyWidth = 200;

    private DialogTextInput passwordInput = DialogTextInput.builder()
            .width(200)
            .label("Enter password")
            .maxLength(32)
            .build();

    private DialogButton confirmAuthButton = DialogButton.builder()
            .label("Confirm Login")
            .tooltip("Click")
            .width(200)
            .build();

    public Dialog createDialog(DialogManager manager, Map<String, ?> placeholders) {
        return new NoticeDialog(
                new CommonDialogData(
                        AdventureColor.of(parsePlaceholders(title, placeholders)),
                        AdventureColor.of(parsePlaceholders(externalTitle, placeholders)),
                        false,
                        false,
                        DialogAction.NONE,
                        List.of(new PlainMessageDialogBody(new PlainMessage(
                                AdventureColor.of(parsePlaceholders(body, placeholders)),
                                bodyWidth
                        ))),
                        List.of(passwordInput
                                .setKey("password")
                                .createInput(placeholders))
                ),
                confirmAuthButton.setClickType(ClickEventType.DYNAMIC_ACTION)
                        .setActionValue("confirm-login")
                        .createActionButton(manager, placeholders)
        );
    }
}
