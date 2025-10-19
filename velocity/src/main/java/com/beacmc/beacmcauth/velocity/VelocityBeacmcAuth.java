package com.beacmc.beacmcauth.velocity;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.core.BaseBeacmcAuth;
import com.beacmc.beacmcauth.core.social.types.vkontakte.VkontakteSocial;
import com.beacmc.beacmcauth.velocity.auth.VelocityPremiumProvider;
import com.beacmc.beacmcauth.velocity.library.VelocityLibraryProvider;
import com.beacmc.beacmcauth.velocity.logger.VelocityServerLogger;
import com.beacmc.beacmcauth.velocity.message.VelocityMessageProvider;
import com.beacmc.beacmcauth.velocity.server.VelocityProxy;
import com.beacmc.beacmcauth.velocity.server.command.*;
import com.beacmc.beacmcauth.velocity.server.listener.AuthListener;
import com.beacmc.beacmcauth.velocity.server.listener.VkontakteListener;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "beacmcauth",
        name = "BeacmcAuth",
        version = "v2.3-BETA",
        authors = {"beacmc"},
        dependencies = {
                @Dependency(id = "luckperms", optional = true),
                @Dependency(id = "vk-api", optional = true),
                @Dependency(id = "packetevents", optional = true),
                @Dependency(id = "azlink", optional = true)
        }
)
public final class VelocityBeacmcAuth {

    private BeacmcAuth beacmcAuth;
    private static VelocityBeacmcAuth instance;
    private final ProxyServer proxyServer;
    private final Path dataDirectory;
    private final Logger logger;

    @Inject
    public VelocityBeacmcAuth(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        beacmcAuth = new BaseBeacmcAuth();
        beacmcAuth.setDataFolder(dataDirectory.toFile())
                .setProxy(new VelocityProxy(proxyServer))
                .setLibraryProvider(new VelocityLibraryProvider(this))
                .setMessageProvider(new VelocityMessageProvider())
                .setServerLogger(new VelocityServerLogger(beacmcAuth))
                .setPremiumProvider(new VelocityPremiumProvider());

        beacmcAuth.onEnable();

        VkontakteSocial vkSocial = (VkontakteSocial) beacmcAuth.getSocialManager().getSocialByType(SocialType.VKONTAKTE);
        if (this.getVelocityProxyServer().getPluginManager().getPlugin("vk-api").isPresent() && vkSocial != null) {
            try {
                Class.forName("com.ubivashka.vk.api.VkApiPlugin");
                Object instance = proxyServer.getPluginManager().getPlugin("vk-api").flatMap(PluginContainer::getInstance).orElse(null);
                vkSocial.setup(instance);
            } catch (Exception ignored) {
            }
            proxyServer.getEventManager().register(this, new VkontakteListener(beacmcAuth));
        }

        proxyServer.getEventManager().register(this, new AuthListener(beacmcAuth));
        initCommands();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (beacmcAuth != null) beacmcAuth.onDisable();
    }

    private void initCommands() {
        final CommandManager commandManager = proxyServer.getCommandManager();

        commandManager.register(
                commandManager.metaBuilder("premium")
                        .plugin(this)
                        .build(),
                new PremiumCommand(beacmcAuth)
        );


        commandManager.register(
                commandManager.metaBuilder("crack")
                        .plugin(this)
                        .build(),
                new CrackCommand(beacmcAuth)
        );

        commandManager.register(
                commandManager.metaBuilder("register")
                        .plugin(this)
                        .aliases("reg")
                        .build(),
                new RegisterCommand(beacmcAuth)
        );

        commandManager.register(
                commandManager.metaBuilder("login")
                        .plugin(this)
                        .aliases("l", "log")
                        .build(),
                new LoginCommand(beacmcAuth)
        );

        commandManager.register(
                commandManager.metaBuilder(beacmcAuth.getConfig().getLinkCommand())
                        .plugin(this)
                        .build(),
                new LinkCommand(beacmcAuth)
        );

        commandManager.register(
                commandManager.metaBuilder("changepassword")
                        .plugin(this)
                        .aliases("cpass", "changepass", "cp")
                        .build(),
                new ChangepasswordCommand(beacmcAuth)
        );

        commandManager.register(
                commandManager.metaBuilder("beacmcauth")
                        .plugin(this)
                        .aliases("auth", "bauth")
                        .build(),
                new AuthCommand(beacmcAuth)
        );
    }

    public Logger getVelocityLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ProxyServer getVelocityProxyServer() {
        return proxyServer;
    }

    public BeacmcAuth getBeacmcAuth() {
        return beacmcAuth;
    }

    public static VelocityBeacmcAuth getInstance() {
        return instance;
    }
}
