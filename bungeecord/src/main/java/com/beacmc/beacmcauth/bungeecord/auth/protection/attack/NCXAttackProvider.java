package com.beacmc.beacmcauth.bungeecord.auth.protection.attack;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.protection.attack.AttackStatusProvider;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.shieldcommunity.nullcordx.api.NullCordX;
import net.shieldcommunity.nullcordx.api.NullCordXApi;
import net.shieldcommunity.nullcordx.api.events.*;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@Getter
@Setter
public class NCXAttackProvider implements AttackStatusProvider, Listener {

    private long attackStartedAt = 0;
    private long lastAttackTime = 0;

    private NullCordX api;

    public NCXAttackProvider(BeacmcAuth plugin) {
        api = NullCordXApi.getInstance();
        plugin.getProxy().registerListener(this);
    }

    @Override
    public boolean isAttacking() {
        return isBotAttackEnabled()
                || isSpamAttackEnabled()
                || isPingAttackEnabled();
    }

    private boolean isBotAttackEnabled() {
        return api.isForceBotAttack() || api.isUnderBotAttack();
    }

    private boolean isSpamAttackEnabled() {
        return api.isForceSpamAttack() || api.isUnderSpamAttack();
    }

    private boolean isPingAttackEnabled() {
        return api.isForcePingAttack() || api.isUnderPingAttack();
    }

    @Override
    public @NotNull Duration getCurrentAttackDuration() {
        return attackStartedAt > 0
                ? Duration.ofMillis(System.currentTimeMillis() - attackStartedAt)
                : Duration.ZERO;
    }

    @Override
    public @NotNull Duration getLastAttackDuration() {
        return lastAttackTime > 0
                ? Duration.ofMillis(lastAttackTime)
                : Duration.ZERO;
    }

    // Attack detected
    @EventHandler
    public void onBotAttack(BotAttackDetectedEvent event) {
        onAttackStart();
    }

    @EventHandler
    public void onSpamAttack(SpamAttackDetectedEvent event) {
        onAttackStart();
    }

    @EventHandler
    public void onPingAttack(PingAttackDetectedEvent event) {
        onAttackStart();
    }

    // Attack ended
    @EventHandler
    public void onBotAttackEnded(BotAttackEndedEvent event) {
        if (!isSpamAttackEnabled() || !isPingAttackEnabled()) {
            onAttackEnded();
        }
    }

    @EventHandler
    public void onSpamAttackEnded(SpamAttackEndedEvent event) {
        if (!isBotAttackEnabled() || !isPingAttackEnabled()) {
            onAttackEnded();
        }
    }

    @EventHandler
    public void onPingAttackEnded(PingAttackEndedEvent event) {
        if (!isBotAttackEnabled() && !isSpamAttackEnabled()) {
            onAttackEnded();
        }
    }

    private void onAttackStart() {
        if (attackStartedAt <= 0) {
            attackStartedAt = System.currentTimeMillis();
        }
    }

    private void onAttackEnded() {
        if (attackStartedAt > 0) {
            lastAttackTime = System.currentTimeMillis() - attackStartedAt;
        }
    }


}
