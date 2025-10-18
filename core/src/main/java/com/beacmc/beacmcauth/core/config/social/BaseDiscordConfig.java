package com.beacmc.beacmcauth.core.config.social;

import com.beacmc.beacmcauth.api.config.social.DiscordConfig;
import com.beacmc.beacmcauth.api.config.social.SocialMessages;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Activity;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseDiscordConfig implements DiscordConfig {

    private String token = "TOKEN";
    private boolean enabled = false;
    private boolean disableUnlink = false;
    private Long guildID = 0L;
    private Integer codeLength = 6;
    private Integer passwordResetLength = 21;
    private Integer timePerConfirm = 60;
    @Comment({"", "Maximum number of links per social network user"})
    private Integer maxLink = 3;
    @Comment({"", "Command prefixes"})
    private String linkCommand = "!link";
    private String accountsCommand = "!accounts";
    @Comment({"", "Characters from which codes and passwords will be created"})
    private String codeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private String resetPasswordChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789#+=-;:@!$%";
    private int messageSendDelaySeconds = 3;
    private boolean activityEnabled = true;
    private Activity.ActivityType activityType = Activity.ActivityType.PLAYING;
    private String activityText = "BeacmcAuth";
    private String activityUrl = "https://twitch.tv/beacmc_";
    @Comment("")
    private Messages messages = new Messages();

    @Getter
    @Configuration
    @SuppressWarnings("FieldMayBeFinal")
    public static class Messages implements SocialMessages {

        String startMessage = "Usage: !link <player>";
        String confirmationMessage = "Your account %name% is being accessed from the following IP address: %ip%\n> Choose an action below";
        String confirmationButtonAcceptText = "Accept";
        String confirmationButtonDeclineText = "Decline";
        String noConfirmation = "No confirmation for logging into the server was received";
        String confirmationSuccess = "You have successfully confirmed logging in to the server";
        String confirmationDenied = "You refused to log on to the server";
        String playerOffline = "The player is not in the game right now";
        String privateChannelOnly = "This command can only be used in private messages with the bot";
        String linkCommandUsage = "Usage: !link <player>";
        String linkCommandPlayerOffline = "Player is offline";
        String linkCommandPlayerNotFound = "Player not found";
        String linkCommandAlreadyLinked = "This account is already linked to Discord";
        String linkLimit = "You have reached the limit of bindings per account";
        String linkMessage = "You have created a code to link the account ***%name%***.\n> `%code%`";
        String chooseAccount = "Choose account please";
        String accountInfo = "> Real name: `%name%` (`%lowercase_name%`)\n> Last Ip Address: ||`%last_ip%`||\n> Register Ip: ||`%reg_ip%||\n\n- Account status: %is_online%";
        String accountResetPasswordButton = "Reset password";
        String accountResetPassword = "You have successfully reset the password on your **%name%** account. \n> New password: ||`%password%`||";
        String accountUnlinkButton = "Unlink account";
        String cooldown = "Wait before the next use";
        String accountAlreadyUnlink = "You've already unlinked your Discord account.";
        String accountUnlinkSuccess = "You have successfully unlinked the account from Discord";
        String unlinkDisabled = "Account unlinking is not possible. Administration has switched off this feature";
        String accountNotLinked = "This account is in no way related to your Discord account";
        String playerInfoOnline = "Online";
        String playerInfoOffline = "Offline";
        String accountKickButton = "Kick";
        String accountKickSuccess = "The account was kicked off the server";
        String account2faToggleButton = "Toggle 2FA";
        String account2faEnabled = "Two-step authorization enabled";
        String account2faDisabled = "Two-step authorization disabled";
        String accountBanToggleButton = "Toggle BAN";
        String accountBanned = "Account successfully banned";
        String accountUnbanned = "Account successfully unbanned";
        String accountsEmpty = "You don't have any linked accounts";
    }
}
