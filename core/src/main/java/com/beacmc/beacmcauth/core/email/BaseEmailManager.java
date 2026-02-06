package com.beacmc.beacmcauth.core.email;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.cache.Cache;
import com.beacmc.beacmcauth.api.config.EmailConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.email.EmailManager;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.core.util.PlaceholderUtil;
import com.j256.ormlite.stmt.SelectArg;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class BaseEmailManager implements EmailManager {

    private final BeacmcAuth plugin;
    private final ExecutorService executorService;
    private final ProtectedPlayerDao dao;
    private final Cache<ProtectedPlayer, UUID> cache;

    public BaseEmailManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.executorService = new ThreadPoolExecutor(
                2,
                4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.dao = plugin.getDatabase().getProtectedPlayerDao();
        this.cache = plugin.getAuthManager().getPlayerCache();
    }

    @Override
    public CompletableFuture<Boolean> sendMailMessage(String to, Map<String, ?> placeholders) {
        if (!isEmail(to)) return CompletableFuture.completedFuture(false);

        final EmailConfig config = plugin.getEmailConfig();

        String mailSubject = PlaceholderUtil.parse(config.getMailSubject(), placeholders);
        String mailText = PlaceholderUtil.parse(config.getMailText(), placeholders);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Message message = new MimeMessage(config.createSession());
                message.setFrom(new InternetAddress(config.getUsername()));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(mailSubject);
                message.setContent(mailText, "text/html; charset=UTF-8");

                Transport.send(message);
                return true;
            } catch (MessagingException e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    @Override
    public boolean isEmail(String email) {
        Pattern pattern = plugin.getEmailConfig().getEmailPattern();
        return email != null && pattern.matcher(email).matches();
    }

    @Override
    public boolean isEmailFree(String email) {
        try {
            if (!isEmail(email)) return false;

            ProtectedPlayer cached = cache.stream()
                    .filter(p -> p.getEmail() != null && p.getEmail().equals(email))
                    .findFirst()
                    .orElse(null);
            if (cached != null) return false;

            ProtectedPlayer query = dao.queryBuilder()
                    .where()
                    .eq("email", new SelectArg(email))
                    .queryForFirst();
            if (query != null) {
                cache.addOrUpdateCache(query);
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
