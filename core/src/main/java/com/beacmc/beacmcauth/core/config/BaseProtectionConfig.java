package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.auth.action.AuthActionTypes;
import com.beacmc.beacmcauth.api.config.AntiBruteForceSettings;
import com.beacmc.beacmcauth.api.config.DangerousCommandConfirmationSettings;
import com.beacmc.beacmcauth.api.config.ProtectionConfig;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    @Comment({"",
            "Adds a confirmation step for commands that remove a player.",
            "from the server, such as: /logout, /crack, and /premium"})
    private BaseDangerousCommandConfirmationSettings dangerousCommandConfirmationSettings
            = new BaseDangerousCommandConfirmationSettings();
    private BaseAntiBruteForceSettings antiBruteForceSettings = new BaseAntiBruteForceSettings();

    @Getter
    @Configuration
    public static class BaseDangerousCommandConfirmationSettings implements DangerousCommandConfirmationSettings {

        private boolean enabled = true;
        private TimeUnit confirmationLifetimeUnit = TimeUnit.MINUTES;
        private long confirmationLifetime = 1;
    }

    @Getter
    @Configuration
    public static class BaseAntiBruteForceSettings implements AntiBruteForceSettings {

        private BaseLimiter accountLimiter = new BaseLimiter(5, 60, 120);
        private BaseLimiter ipLimiter = new BaseLimiter(10, 60, 30);
        private BaseDistributed distributed = new BaseDistributed();

        @Getter
        @Configuration
        @AllArgsConstructor
        public static class BaseLimiter implements AntiBruteForceSettings.Limiter {

            private int maxFailures;
            private int trackingPeriodSeconds;
            private int lockDurationSeconds;
        }

        @Getter
        @Configuration
        public static class BaseDistributed implements AntiBruteForceSettings.Distributed {

            private boolean enabled = true;
            private int uniqueIpsThreshold = 5;
            private int trackingPeriodSeconds = 120;
        }
    }
}
