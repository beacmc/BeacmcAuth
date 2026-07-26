package com.beacmc.beacmcauth.api.event.type;

import com.beacmc.beacmcauth.api.event.Event;
import com.beacmc.beacmcauth.api.event.EventListener;
import com.beacmc.beacmcauth.api.event.EventType;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
public class ChangePasswordEvent extends Event {

    private final String oldPasswordHash;

    public ChangePasswordEvent(
            @NotNull ProtectedPlayer protectedPlayer,
            @NotNull ServerPlayer serverPlayer,
            @NotNull String oldPasswordHash) {
        super(EventType.CHANGE_PASSWORD, protectedPlayer, serverPlayer);
        this.oldPasswordHash = oldPasswordHash;
    }

    @Override
    public void handle(EventListener listener) {
        listener.onChangePassword(this);
    }
}
