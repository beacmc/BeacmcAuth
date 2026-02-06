package com.beacmc.beacmcauth.core.config;

import com.beacmc.beacmcauth.api.config.EmailConfig;
import de.exlll.configlib.Configuration;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class BaseEmailConfig implements EmailConfig {

    private boolean enabled = false;
    private Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private String smtpHost = "smtp.gmail.com";
    private int smtpPort = 485;
    private boolean smtpAuthEnabled = true;
    private boolean sslEnabled = true;
    private String username = "username@gmail.com";
    private String password = "password";
    private String mailSubject = "Account recovery";
    private String mailText = """
             <h2 style='color:#ffbb00;'>Привет, %player%!</h2>
             <p>Твой <b>новый пароль</b> для аккаунта: %password%</p>
            """;
    private TimeUnit recoveryCooldownTimeUnit = TimeUnit.HOURS;
    private long recoveryCooldownValue = 1;
}
