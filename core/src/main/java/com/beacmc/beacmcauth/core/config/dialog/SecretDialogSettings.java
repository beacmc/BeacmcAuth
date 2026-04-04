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
public class SecretDialogSettings implements DialogCreator {

    private String title = "Secret Recovery";
    private String externalTitle = null;
    private String body = """
            &fPlease answer the question:
            &#ffbb00%secret_question%?
            """;
    private int bodyWidth = 200;

    private DialogTextInput answerInput = DialogTextInput.builder()
            .width(200)
            .label("Enter answer")
            .maxLength(64)
            .build();

    private DialogButton confirmAuthButton = DialogButton.builder()
            .label("Confirm Recovery")
            .tooltip("Click")
            .width(200)
            .build();

    @Override
    public Dialog createDialog(DialogManager manager, Map<String, ?> placeholders) {
        List<Input> passwordInputs = new ArrayList<>();
        passwordInputs.add(answerInput
                .setKey("answer")
                .createInput(placeholders));

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
                        .setActionValue("confirm-secret-recovery")
                        .createActionButton(manager, placeholders)
        );
    }
}
