package com.beacmc.beacmcauth.api.social.keyboard.button;

import com.beacmc.beacmcauth.api.PlaceholderSupport;
import de.exlll.configlib.Configuration;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Configuration
public class Button implements PlaceholderSupport {

    private String label;
    private String callbackData;
    private ButtonType type;

    public Button parseButtonPlaceholders(Map<String, ?> placeholders) {
        Button button = new Button(label, callbackData, type);
        if (placeholders != null) {
            button.setLabel(parsePlaceholders(label, placeholders));
            button.setCallbackData(parsePlaceholders(callbackData, placeholders));
        }
        return button;
    }
}
