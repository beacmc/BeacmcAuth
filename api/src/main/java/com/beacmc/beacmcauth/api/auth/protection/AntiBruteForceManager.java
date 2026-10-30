package com.beacmc.beacmcauth.api.auth.protection;

import com.beacmc.beacmcauth.api.auth.protection.model.AttemptResult;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;

public interface AntiBruteForceManager {

    AttemptResult recordFailedAttempt(String playerName, String ip);

    default AttemptResult recordFailedAttempt(ServerPlayer player) {
        return recordFailedAttempt(player.getLowercaseName(), player.getInetAddress().getHostAddress());
    }

    void clearAccountFailures(String playerName);

    default void clearAccountFailures(ServerPlayer player) {
        clearAccountFailures(player.getLowercaseName());
    }

    void cleanupExpired();

    boolean isIpLocked(String ip);

    default boolean isIpLocked(ServerPlayer player) {
        return isIpLocked(player.getInetAddress().getHostAddress());
    }

    boolean isAccountLocked(String playerName);

    default boolean isAccountLocked(ServerPlayer player) {
        return isAccountLocked(player.getLowercaseName());
    }

    default boolean isAccountLocked(ProtectedPlayer player) {
        return isAccountLocked(player.getLowercaseName());
    }
}
