package com.beacmc.beacmcauth.core.config.social.settings;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.social.settings.KeyboardsSettings;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;
import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import com.beacmc.beacmcauth.api.social.keyboard.button.ButtonType;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseKeyboardsSettings implements KeyboardsSettings {

    private BaseAccountListKeyboardSettings accountListKeyboardSettings = new BaseAccountListKeyboardSettings();
    private Keyboard accountConfirmationKeyboard = Keyboard.builder()
            .buttons(List.of(List.of(
                    Button.builder()
                            .type(ButtonType.SUCCESS)
                            .label("Accept")
                            .callbackData("confirm-accept:")
                            .build(),
                    Button.builder()
                            .type(ButtonType.DANGER)
                            .label("Reject")
                            .callbackData("confirm-decline:")
                            .build()
            )))
            .build();
    private Keyboard accountManageKeyboard = Keyboard.builder()
            .buttons(List.of(
                            List.of(
                                    Button.builder()
                                            .label("Toggle 2FA")
                                            .callbackData("toggle-2fa:{name}")
                                            .type(ButtonType.SECONDARY)
                                            .build(),
                                    Button.builder()
                                            .label("Reset password")
                                            .callbackData("reset-password:{name}")
                                            .type(ButtonType.SECONDARY)
                                            .build()
                            ),
                            List.of(
                                    Button.builder()
                                            .label("Toggle ban")
                                            .callbackData("toggle-ban:{name}")
                                            .type(ButtonType.SECONDARY)
                                            .build(),
                                    Button.builder()
                                            .label("Kick")
                                            .callbackData("kick:{name}")
                                            .type(ButtonType.SECONDARY)
                                            .build()
                            ),
                            List.of(
                                    Button.builder()
                                            .label("Unlink")
                                            .callbackData("unlink:{name}")
                                            .type(ButtonType.DANGER)
                                            .build()
                            )
                    )
            )
            .build();

    @Override
    public Keyboard createAccountManageKeyboard(ProtectedPlayer protectedPlayer) {
        return accountManageKeyboard.parsePlaceholders(Map.of("{name}", protectedPlayer.getLowercaseName()));
    }

    @Override
    public Keyboard createConfirmationKeyboard(ProtectedPlayer protectedPlayer) {
        return accountConfirmationKeyboard.parsePlaceholders(Map.of("{name}", protectedPlayer.getLowercaseName()));
    }

    @Override
    public @Nullable Keyboard createAccountsListKeyboard(List<ProtectedPlayer> protectedPlayers, int page) {
        Keyboard keyboard = new Keyboard();
        List<List<Button>> buttons = new ArrayList<>();
        buttons.add(accountListKeyboardSettings.getHeaderButtons().stream()
                .map(button -> button.parsePlaceholders(Map.of("{page}", page)))
                .toList());

        final int start = page * 4;
        final int end = Math.min(4, protectedPlayers.size());
        final List<ProtectedPlayer> pageContent = protectedPlayers.subList(start, end);

        if (pageContent.isEmpty()) {
            return null;
        }

        pageContent.forEach(account -> buttons.add(List.of(
                accountListKeyboardSettings.getAccountButton().parsePlaceholders(Map.of(
                        "{name}", account.getRealName(),
                        "{lowercase_name}", account.getLowercaseName()
                ))
        )));

        buttons.add(accountListKeyboardSettings.getFooterButtons().stream()
                .map(button -> button.parsePlaceholders(Map.of("{page}", page)))
                .toList());
        return keyboard.setButtons(buttons);
    }

    @Getter
    @Configuration
    @NoArgsConstructor
    @SuppressWarnings("FieldMayBeFinal")
    public static class BaseAccountListKeyboardSettings implements AccountListKeyboardSettings {

        private int accountsInOnePage = 4;
        private Button accountButton = Button.builder()
                .label("{name}")
                .type(ButtonType.SECONDARY)
                .callbackData("account:{lowercase_name}")
                .build();
        private List<Button> headerButtons = List.of();
        private List<Button> footerButtons = List.of(
                Button.builder()
                        .type(ButtonType.PRIMARY)
                        .label("⏩")
                        .callbackData("next:{page}")
                        .build(),
                Button.builder()
                        .label("⏪")
                        .callbackData("previous:{page}")
                        .type(ButtonType.PRIMARY)
                        .build()

        );
    }
}
