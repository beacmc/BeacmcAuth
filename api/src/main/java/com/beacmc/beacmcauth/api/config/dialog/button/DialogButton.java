package com.beacmc.beacmcauth.api.config.dialog.button;

import com.beacmc.beacmcauth.api.AdventureColor;
import com.beacmc.beacmcauth.api.PlaceholderSupport;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.github.retrooper.packetevents.protocol.chat.clickevent.OpenUrlClickEvent;
import com.github.retrooper.packetevents.protocol.chat.clickevent.RunCommandClickEvent;
import com.github.retrooper.packetevents.protocol.chat.clickevent.ShowDialogClickEvent;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.action.Action;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.action.StaticAction;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.Ignore;
import lombok.*;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
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
public class DialogButton implements PlaceholderSupport {

    @Ignore
    private static final String NAMESPACE = "beacmcauth";

    private String label;
    private String tooltip;
    private int width;
    private @Ignore ClickEventType clickType;
    private @Nullable @Ignore String actionValue;
    private @Nullable @Ignore String command;
    private @Nullable @Ignore String showDialogId;
    private @Nullable @Ignore String url;

    public ActionButton createActionButton(DialogManager manager, @Nullable Map<String, ?> placeholders) {
        CommonButtonData commonButtonData = new CommonButtonData(
                AdventureColor.of(parsePlaceholders(label, placeholders)),
                AdventureColor.of(parsePlaceholders(tooltip, placeholders)),
                width
        );

        Action action = switch (clickType) {
            case DYNAMIC_ACTION -> new DynamicCustomAction(
                    new ResourceLocation(Key.key(NAMESPACE, actionValue != null ? actionValue : "none")),
                    null
            );
            case PLAYER_COMMAND -> command != null ? new StaticAction(new RunCommandClickEvent(command)) : null;
            case OPEN_URL -> new StaticAction(new OpenUrlClickEvent(url != null ? url : "https://nometa.xyz"));
            case SHOW_DIALOG -> {
                Dialog dialog = showDialogId != null ? manager.getDialog(showDialogId) : null;
                if (dialog == null) yield null;

                yield new StaticAction(new ShowDialogClickEvent(dialog));
            }
            case null -> null;
        };

        return new ActionButton(commonButtonData, action);
    }

    public ActionButton createActionButton(DialogManager manager) {
        return createActionButton(manager, null);
    }
}
