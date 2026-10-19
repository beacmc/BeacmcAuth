package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.auth.action.AuthActionTypes;
import com.beacmc.beacmcauth.api.config.ProtectionConfig;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseProtectionConfig implements ProtectionConfig {


    private List<String> exemptPlayers = List.of("beacmc");
    @Comment({
            "",
            "What do these blocks do?",
            "",
            "login -> blocks any access to the server.",
            "register -> blocks access for unregistered players.",
            "change-password -> prevents changing the password via the \"/changepass\" command.",
            "premium-authentication -> automatically switches licensed users to a non-licensed mode.",
            "open-auth-dialog -> Instead of authorization via dialog boxes, the player is offered the option to authorize using commands.",
            "",
            "true - blocked",
            "false - unblocked",
            "NOTE: The bans will not apply to users specified in exemptPlayers."
    })
    private Map<String, Boolean> blockedActionsByDefault = Map.of(
            AuthActionTypes.LOGIN.getIdentifier(), false,
            AuthActionTypes.REGISTER.getIdentifier(), false,
            AuthActionTypes.CHANGE_PASSWORD.getIdentifier(), false,
            AuthActionTypes.PREMIUM_AUTHENTICATION.getIdentifier(), false,
            AuthActionTypes.OPEN_AUTH_DIALOG.getIdentifier(), false
    );
    private Map<String, Boolean> blockedActionsOnAttack = Map.of(
            AuthActionTypes.LOGIN.getIdentifier(), false,
            AuthActionTypes.REGISTER.getIdentifier(), false,
            AuthActionTypes.CHANGE_PASSWORD.getIdentifier(), false,
            AuthActionTypes.PREMIUM_AUTHENTICATION.getIdentifier(), false,
            AuthActionTypes.OPEN_AUTH_DIALOG.getIdentifier(), true
    );
}
