package com.beacmc.beacmcauth.api.social.keyboard.button;

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
public class Button {

    String label;
    String callbackData;
    ButtonType type;

    public Button parsePlaceholders(Map<String, ?> placeholders) {
        Button button = new Button(label, callbackData, type);
        if (placeholders != null) {
            for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
                button.setLabel(button.getLabel().replace(entry.getKey(), String.valueOf(entry.getValue())));
                button.setCallbackData(button.getCallbackData().replace(entry.getKey(), String.valueOf(entry.getValue())));
            }
        }
        return button;
    }
}
