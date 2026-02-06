package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ConfigMessages;
import com.beacmc.beacmcauth.api.server.Server;
import de.exlll.configlib.Comment;
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
    @Comment({"", "Read more on the website: https://azuriom.com"})
    private boolean azLinkIntegration = false;
    private Integer timePerLogin = 60;
    private Integer timePerRegister = 60;
    private int registerMessageSendDelaySeconds = 3;
    private int loginMessageSendDelaySeconds = 3;
    @Comment({"", "A parameter of the bcrypt password hashing algorithm that determines", "the computational cost and, consequently, the time required to compute the hash."})
    private Integer bCryptRounds = 12;
    private Integer passwordAttempts = 3;

    @Comment({"", "Time unit types: DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS"})
    private TimeUnit premiumCacheTimeUnit = TimeUnit.HOURS;
    private long premiumCacheTimeUnitValue = 1;
    private TimeUnit lifetimeOfTemporaryPremiumVerificationTimeUnit = TimeUnit.MINUTES;
    private long lifetimeOfTemporaryPremiumVerificationTimeUnitValue = 3;

    @Comment({"", "Authorization servers and server lobby.", "Creation format: server-name:maximum-online"})
    private List<String> authServers = List.of("auth-1:100", "auth-2:100");
    private List<String> lobbyServers = List.of("lobby-1:100", "lobby-2:100");
    @Comment("")
    private String linkCommand = "link";
    @Comment({"", "Servers that cannot be accessed during authorization.", "Please observe the case sensitivity of server names!"})
    private List<String> disabledServers = List.of("anarchy-1", "anarchy-2");
    @Comment({"", "Commands available upon authorization and registration"})
    private List<String> whitelistCommands = List.of("/l", "/log", "/login", "/reg", "/register", "/recovery");
    private boolean nameCaseControl = true;
    private Pattern nicknameRegex = Pattern.compile("[a-zA-Z0-9_]*");
    private boolean registerRepeatPassword = true;
    private Pattern passwordRegex = Pattern.compile("^[A-Za-z0-9!@#$%&*()\\-_=+\\[\\]{};:'\",.<>/?`~\\\\|]{8,64}$");
    @Comment({"", "PacketEvents required"})
    private boolean nbsSongSupport = true;
    private String recoveryPasswordChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private BaseDatabaseSettings databaseSettings = new BaseDatabaseSettings();
    private BaseAccountLimiterSettings accountLimiterSettings = new BaseAccountLimiterSettings();
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
        String invalidPassword = "&7The password does not meet the criteria. Between 8 and 64 characters";
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
        String notAuthed = "&7You're not logged in";
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

        String logoutDisconnect = """
                &6Your session was successfully reset
                """;
        String altsCommandUsage = "&7Usage: /alts <player>";
        String altsAccountsEmpty = "&7No alternative accounts found";
        String alternativeAccountsLimitReached = "&7the limit of alternative accounts has been reached";
        String secretAnswerAlreadyCreated = "&7You have already created a secret answer.";
        String secretAnswerCreated = "&7Your secret answer has been successfully created!";
        String secretAnswerNotCreated = "&7You have not created a secret answer yet.";
        String invalidSecretAnswerDisconnect = "&cInvalid secret answer. You have been disconnected.";
        String secretAnswerSuccessUsedDisconnect = "&7Secret answer accepted. New password: &6%password%";
        String secretAnswerRemoved = "&7A secret question has been successfully removed";
        String secretSetCommandUsage = "&7Usage: /secret set <question>? <answer>";
        String secretInvalidSyntax = "&7Invalid syntax, usage: <question>? <answer>";
        String secretCommandUsage = "&7Usage: /secret <set/remove/recovery>";
        String answerTooLong = "&7The answer is too long.";
        String secretRecovery = """
                &7Your question: &6%question%?
                &7Enter the command &6/secret recovery <response>
                """;
        String emailCommandUsage = "&7Usage: /email <add/remove/recovery>";
        String emailAddCommandUsage = "&7Usage: /email add <username@gmail.com>";
        String emailRecoveryCommandUsage = "&7Usage: /email recovery <your-email>";
        String emailRecoveryInvalidDisconnect = "&cThe email address is incorrect!";
        String emailAdded = "&7Mail successfully added";
        String emailRemoved = "&7Mail successfully removed";
        String emailInvalid = "&7Invalid syntax. Example: username@gmail.com";
        String emailNotAdded = "&7Email not added yet.";
        String emailAlreadyAdded = "&7Email already added.";
        String couldNotSendEmailMessages = "&7An error occurred while sending an email";
        String emailAlreadyTaken = "&7This email is already taken";
        String emailRecoveryDisconnect = """
                &7You have been disconnected from the server due to a password change
                &6Your new password is waiting for you in your Email
                """;
    }
}
