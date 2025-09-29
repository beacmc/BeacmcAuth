package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.server.Server;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.Ignore;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseConfig implements Config {

    private Integer sessionTime = 60;
    private boolean debugEnabled = false;
    private boolean azLinkIntegration = false;
    private Integer timePerLogin = 60;
    private Integer timePerRegister = 60;
    private Integer bCryptRounds = 12;
    private Integer passwordMinLength = 8;
    private Integer passwordMaxLength = 72;
    private Integer passwordAttempts = 3;
    private TimeUnit premiumCacheTimeUnit = TimeUnit.HOURS;
    private long premiumCacheTimeUnitValue = 1;
    private TimeUnit lifetimeOfTemporaryPremiumVerificationTimeUnit = TimeUnit.MINUTES;
    private long lifetimeOfTemporaryPremiumVerificationTimeUnitValue = 3;
    private List<String> authServers = List.of("auth-1:100", "auth-2:100");
    private String linkCommand = "link";
    private List<String> disabledServers = List.of("anarchy-1", "anarchy-2");
    private List<String> lobbyServers = List.of("lobby-1:100", "lobby-2:100");
    private List<String> whitelistCommands = List.of("/l", "/log", "/login", "/reg", "/register");
    private boolean nameCaseControl = true;
    private Pattern nicknameRegex = Pattern.compile("[a-zA-Z0-9_]*");
    private BaseDatabaseSettings databaseSettings = new BaseDatabaseSettings();
    private Messages messages = new Messages();

    @Ignore
    private BeacmcAuth plugin;

    @Override
    public Server findServer(List<String> configServers) {
        for (String configServer : configServers) {
            String[] args = configServer.split(":");
            Server serverInfo = plugin.getProxy().getServer(args[0]);
            if (serverInfo == null) {
                plugin.getServerLogger().error("Server " + args[0] + " not found");
                continue;
            }
            int maxPlayers = args.length >= 2 ? Integer.parseInt(args[1]) : Integer.MAX_VALUE;
            int players = serverInfo.getOnlinePlayersSize();

            if (players < maxPlayers) {
                return serverInfo;
            }
        }
        return null;
    }

    public BaseConfig setPlugin(BeacmcAuth plugin) {
        this.plugin = plugin;
        return this;
    }

    @Getter
    @Configuration
    @SuppressWarnings("FieldMayBeFinal")
    public static class Messages implements ConfigMessages {

        String registerChat = "&7Register: &#ffbb00/reg &7(&#ffbb00&lpassword&r&7) &7(&#ffbb00&lpassword&r&7)";
        String registerTitle = "&7";
        String registerSubtitle = "&7Register an account";
        String passwordsDontMatch = "&7The passwords don't match";
        String lowCharacterPassword = "&7There are not enough characters in the password";
        String highCharacterPassword = "&7There are too many characters in the password";
        String notRegistered = "&7You're not registered";
        String registerSuccess = "&7Registration successful";
        String confirmPassword = "&7Repeat your password in the second argument";
        String alreadyRegister = "&7You are already registered";
        String loginChat = "&7Login: &#ffbb00/login &7(&#ffbb00&lpassword&r&7)";
        String loginTitle = "&7";
        String loginSubtitle = "&7Login to your account";
        String wrongPassword = "&7The password is wrong. Attempts left: %attempts%";
        String attemptsLeft = "&cAttempts to enter the password have been exhausted";
        String loginSuccess = "&7Authorization successful";
        String changePasswordCommandUsage = "&7Usage: /cp <old-password> <new-password>";
        String oldPasswordWrong = "&7The old password is incorrect";
        String changePasswordSuccess = "&7You have successfully changed your old password to a new one";
        String passwordsMatch = "&7Your old password and your new password are identical";
        String authReload = "&7Configuration rebooted";
        String accountNotFound = "&7Account not found";
        String accountDeleted = "&7Account not found";
        String accountNotRegistered = "&7Account not yet registered";
        String yourAccountDeletedDisconnect = "&cYour account has been deleted";
        String accountPasswordChanged = "&7Account password successfully changed";
        String authHelp = """
                #ffbb00&lBeacmcAuth commands
                #ffbb00&m       &r
                &7auth 
                &7 - #ffbb00reload &8:&7 reload config
                &7 - #ffbb00delete <account> &8:&7 delete account
                &7 - #ffbb00changepass <account> <pass> &8:&7 change account password
                """;
        String alreadyAuthed = "&7You are already logged in";
        String enterPassword = "&7You have not entered your password";
        String sessionActive = "&7Server recognized you, authorization is not required";
        String invalidCharacterInName = "7Invalid characters are detected in the name";
        String findServerError = "&cAll servers are full or there is no communication with them. Please wait...";
        String timeIsUp = "&cAuthorization time is up, try again";
        String blockedServer = "&cAuthorize to connect to this server";
        String cooldown = "&cWait before the next use";
        String internalError = """
                &c&lWARNING!!!
                &fThere was an internal error. Please contact the administration.
                """;

        String nameCaseFailed = """
                &7Your nickname does not match the nickname you registered under. 
                &7Current name: &#ffbb00%current_name%
                &7Required name: &#ffbb00%need_name%
                """;

        String linkCodeAbsent = "&7No code has been sent to your account yet";
        String alreadyLinked = "&7Your account is already linked to this social network";
        String linkCommandUsage = "&7Usage: &#ffbb00/link <code>";
        String incorrectCode = "&7You have entered the wrong code for the binding";
        String linkSuccess = "&7You have successfully linked your account";
        String accountBanned = "&cAccount blocked via a social network";

        String alreadyPremium = "&7Your account already has Premium status";
        String alreadyCrack = "&7Your account already has Crack status";
        String premiumSuccess = "&7You have successfully upgraded your status to premium.";
        String crackSuccess = "&7You have successfully upgraded your status to crack.";
        String premiumAccountNotFound = "&7No premium account found for your nickname.";
        String premiumAccountAutoLogin = "&7You have automatically logged in using your premium account.";

        String discordPrivateMessagesClosed = "&cYour private messages are closed. It is not possible to enter the game";
        String discordConfirmationChat = "&7Confirm account login in your discord's private messages";
        String discordConfirmationTitle = "&7";
        String discordConfirmationSubtitle = "&7Confirm the discord login";
        String discordConfirmationDeniedDisconnect = "&cEntry denied in discord's private messages";
        String discordConfirmationSuccess = "&7Login confirmed, you will be redirected to the server";
        String discordKick = "&cYou were kicked at the discord request";

        String telegramPrivateMessagesClosed = "&cYour private messages are closed. It is not possible to enter the game";
        String telegramConfirmationChat = "&7Confirm account login in your telegram's private messages";
        String telegramConfirmationTitle = "&7";
        String telegramConfirmationSubtitle = "&7Confirm the telegram login";
        String telegramConfirmationDeniedDisconnect = "&cEntry denied in telegram's private messages";
        String telegramConfirmationSuccess = "&7Login confirmed, you will be redirected to the server";
        String telegramKick = "&cYou were kicked at the telegram request";

        String vkontaktePrivateMessagesClosed = "&cYour private messages are closed. It is not possible to enter the game";
        String vkontakteConfirmationChat = "&7Confirm account login in your vkontakte's private messages";
        String vkontakteConfirmationTitle = "&7";
        String vkontakteConfirmationSubtitle = "&7Confirm the vkontakte login";
        String vkontakteConfirmationDeniedDisconnect = "&cEntry denied in vkontakte's private messages";
        String vkontakteConfirmationSuccess = "&7Login confirmed, you will be redirected to the server";
        String vkontakteKick = "&cYou were kicked at the vkontakte request";

    }
}
