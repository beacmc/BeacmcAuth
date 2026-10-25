package com.beacmc.beacmcauth.api.auth.protection;

import java.util.UUID;

public record AwaitingDangerousCommand(UUID uuid, String command, long expiresAt) {

    public boolean isValid() {
        return expiresAt > System.currentTimeMillis();
    }
}
