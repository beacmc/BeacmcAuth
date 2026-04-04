package com.beacmc.beacmcauth.api.config.dialog.input;

import com.beacmc.beacmcauth.api.AdventureColor;
import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.PlaceholderSupport;
import com.github.retrooper.packetevents.protocol.dialog.input.Input;
import com.github.retrooper.packetevents.protocol.dialog.input.TextInputControl;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.Ignore;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DialogTextInput implements PlaceholderSupport {

    private int width;
    private String label;
    int maxLength;
    @Ignore String key;

    public Input createInput(@Nullable Map<String, ?> placeholders) {
        TextInputControl control = new TextInputControl(
                width,
                AdventureColor.of(parsePlaceholders(label, placeholders)),
                true,
                "",
                maxLength,
                null
        );

        return new Input(key, control);
    }

    public Input createInput() {
        return createInput(null);
    }
}
