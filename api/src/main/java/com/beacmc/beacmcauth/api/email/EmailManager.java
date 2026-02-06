package com.beacmc.beacmcauth.api.email;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface EmailManager {

    CompletableFuture<Boolean> sendMailMessage(String to, Map<String, ?> placeholders);

    boolean isEmail(String email);

    boolean isEmailFree(String email);
}
