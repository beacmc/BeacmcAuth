package com.beacmc.beacmcauth.api.social.keyboard;

import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import de.exlll.configlib.Configuration;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Keyboard {

    List<List<Button>> buttons;

    public Keyboard parsePlaceholders(Map<String, ?> placeholders) {
        return new Keyboard(buttons.stream()
                .map(buttonsCopy -> buttonsCopy.stream()
                        .map(button -> button.parseButtonPlaceholders(placeholders))
                        .toList())
                .toList());
    }
}
