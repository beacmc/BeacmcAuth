package com.beacmc.beacmcauth.api.social;

import com.beacmc.beacmcauth.api.cache.cooldown.AbstractCooldown;
import com.beacmc.beacmcauth.api.config.social.SocialConfig;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.keyboard.Keyboard;

import java.util.List;

public interface Social<T, ID> {

    boolean isEnabled();

    boolean isInit();

    Object createKeyboard(Keyboard keyboard);

    T getOriginalSocial();

    SocialType getType();

    boolean isPlayerTwoFaEnabled(ProtectedPlayer player);

    void switchPlayerTwoFa(ProtectedPlayer player, boolean enabled);

    boolean isPlayerLinked(ProtectedPlayer player);

    void linkPlayer(ProtectedPlayer player, Object id);

    void unlinkPlayer(ProtectedPlayer player);

    String getLinkCommandPrefix();

    SocialConfig getSocialConfig();

    AbstractCooldown<ID> getCooldownCache();

    boolean isCooldown(Object id);

    void createCooldown(Object id, long time);

    boolean startConfirmation(ProtectedPlayer player);

    List<ProtectedPlayer> getLinkedPlayersById(Object id);
}
