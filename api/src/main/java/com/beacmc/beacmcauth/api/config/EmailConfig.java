package com.beacmc.beacmcauth.api.config;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public interface EmailConfig {

    boolean isEnabled();

    Pattern getEmailPattern();

    String getSmtpHost();

    int getSmtpPort();

    boolean isSmtpAuthEnabled();

    boolean isSmtpDebug();

    boolean isSslEnabled();

    boolean isTslEnabled();

    String getUsername();

    String getPassword();

    String getMailSubject();

    String getMailText();

    TimeUnit getRecoveryCooldownTimeUnit();

    long getRecoveryCooldownValue();

    default long getRecoveryCooldownMillis() {
        return getRecoveryCooldownTimeUnit().toMillis(getRecoveryCooldownValue());
    }

    default Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", getSmtpHost());
        properties.put("mail.smtp.port", getSmtpPort());
        properties.put("mail.smtp.auth", isSmtpAuthEnabled());
        if (isSslEnabled() && !isTslEnabled()) {
            properties.put("mail.smtp.ssl.enable", "true");
        }
        if (!isSslEnabled() && isTslEnabled()) {
            properties.put("mail.smtp.starttls.enable", "true");
        }
        properties.put("mail.transport.protocol", "smtp");
        return properties;
    }

    default Session createSession() {
        Session session = Session.getInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUsername(), getPassword());
            }
        });
        session.setDebug(isSmtpDebug());
        return session;
    }
}
