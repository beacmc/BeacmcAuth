package com.beacmc.beacmcauth.core.config.dialog;

import com.beacmc.beacmcauth.api.AdventureColor;
import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.dialog.button.ClickEventType;
import com.beacmc.beacmcauth.api.config.dialog.button.DialogButton;
import com.beacmc.beacmcauth.api.config.dialog.input.DialogTextInput;
import com.beacmc.beacmcauth.api.dialog.DialogCreator;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.github.retrooper.packetevents.protocol.dialog.*;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.input.Input;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class RegisterDialogSettings implements DialogCreator {

    private String title = "Register";
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

    private DialogTextInput repeatPasswordInput = DialogTextInput.builder()
            .width(200)
            .label("Repeat password")
            .maxLength(32)
            .build();

    private DialogButton confirmAuthButton = DialogButton.builder()
            .label("Confirm Register")
            .tooltip("Click")
            .width(200)
            .build();

    @Override
    public Dialog createDialog(DialogManager manager, Map<String, ?> placeholders) {
        throw new IllegalStateException();
    }

    public Dialog createDialog(DialogManager manager, Map<String, ?> placeholders, boolean repeatPassword) {
        List<Input> passwordInputs = new ArrayList<>();
        passwordInputs.add(passwordInput
                .setKey("password")
                .createInput(placeholders));

        if (repeatPassword) {
            passwordInputs.add(repeatPasswordInput
                    .setKey("repeatPassword")
                    .createInput(placeholders));
        }

        return new NoticeDialog(
                new CommonDialogData(
                        AdventureColor.of(title),
                        AdventureColor.of(externalTitle),
                        false,
                        false,
                        DialogAction.NONE,
                        List.of(new PlainMessageDialogBody(new PlainMessage(
                                AdventureColor.of(parsePlaceholders(body, placeholders)),
                                bodyWidth
                        ))),
                        passwordInputs
                ),
                confirmAuthButton.setClickType(ClickEventType.DYNAMIC_ACTION)
                        .setActionValue("confirm-register")
                        .createActionButton(manager, placeholders)
        );
    }
}

