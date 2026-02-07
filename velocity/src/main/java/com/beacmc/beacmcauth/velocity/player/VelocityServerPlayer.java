package com.beacmc.beacmcauth.velocity.player;

import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import com.beacmc.beacmcauth.velocity.server.VelocityServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.title.Title;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.UUID;

public class VelocityServerPlayer implements ServerPlayer {

    private final Player player;
    private final MessageProvider messageProvider;
    private final ServerLogger logger;;

    public VelocityServerPlayer(Player player) {
        this.player = player;
        this.messageProvider = VelocityBeacmcAuth.getInstance().getBeacmcAuth().getMessageProvider();
        this.logger = VelocityBeacmcAuth.getInstance().getBeacmcAuth().getServerLogger();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public void sendMessage(String message) {
        if (isConnected()) {
            logger.debug("Send message to player(" + player.getUsername() + "): " + message);
            player.sendMessage(messageProvider.createMessage(message).toComponent());
        }
    }

    @Override
    public boolean isConnected() {
        return player.isActive() && player.getCurrentServer().isPresent();
    }


    @Override
    public void connect(Server server) {
        logger.debug("Create connection request. Player(" + player.getUsername() + "), Server(" + server.getName() + ")");
        player.createConnectionRequest(server.getOriginalServer()).connect().whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.warn("%s failed connect to server %s".formatted(player.getUsername(), server.getName()));
                logger.warn(throwable.getMessage());
                return;
            }

            switch (result.getStatus()) {
                case SUCCESS:
                case ALREADY_CONNECTED:
                case CONNECTION_IN_PROGRESS:
                    break;

                case SERVER_DISCONNECTED:
                case CONNECTION_CANCELLED:
                    logger.warn("%s failed connect to server %s".formatted(player.getUsername(), server.getName()));
                    break;
            }
        });
    }

    @Override
    public void disconnect(String message) {
        logger.debug("Player( "+ player.getUsername() + ") disconnect for message: " + message);
        player.disconnect(messageProvider.createMessage(message).toComponent());
    }

    @Override
    public void sendTitle(String title, String subtitle, long in, long stay, long out) {
        if (!isConnected()) return;

        logger.debug("send title to player(" + player.getUsername() + "); title: " + title + "; subtitle: " + subtitle + "; fadeIn:" + in + "; stay:" + stay + "; fadeOut:" + out);
        player.showTitle(Title.title(
                messageProvider.createMessage(title).toComponent(),
                messageProvider.createMessage(subtitle).toComponent(),
                Title.Times.times(
                        Duration.ofSeconds(in),
                        Duration.ofSeconds(stay),
                        Duration.ofSeconds(out)
                )
        ));
    }

    @Override
    public InetAddress getInetAddress() {
        InetSocketAddress socket = player.getRemoteAddress();
        return socket != null ? socket.getAddress() : null;
    }

    @Override
    public <T> T getOriginalPlayer() {
        return (T) player;
    }

    @Override
    public boolean hasPermission(String perm) {
        boolean result = player.hasPermission(perm);
        logger.debug("Check permission(" + perm + ") for player(" + player.getUsername() + "). Result: " + result);
        return result;
    }

    @Override
    public Server getCurrentServer() {
        ServerConnection connection = player.getCurrentServer().orElse(null);
        return connection != null ? new VelocityServer(connection.getServer()) : null;
    }

    @Override
    public String toString() {
        return player.getUsername();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return obj.equals(getName());
        }
        return super.equals(obj);
    }
}
