package com.beacmc.beacmcauth.api;

import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumProvider;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.social.DiscordConfig;
import com.beacmc.beacmcauth.api.config.social.TelegramConfig;
import com.beacmc.beacmcauth.api.config.social.VkontakteConfig;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.packet.PlayerPositionTracker;
import com.beacmc.beacmcauth.api.server.Proxy;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.song.SongManager;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

public interface BeacmcAuth {

    BeacmcAuth onEnable();

    BeacmcAuth onDisable();

    Config getConfig();

    Database getDatabase();

    AuthManager getAuthManager();

    void reloadAllConfigurations();

    MessageProvider getMessageProvider();

    BeacmcAuth setMessageProvider(MessageProvider messageProvider);

    Proxy getProxy();

    TelegramConfig getTelegramConfig();

    VkontakteConfig getVkontakteConfig();

    File getDataFolder();

    DiscordConfig getDiscordConfig();

    void saveResource(String file);

    SocialManager getSocialManager();

    InputStream getResource(String file);

    BeacmcAuth setDataFolder(File file);

    ServerLogger getServerLogger();

    CommandManager getCommandManager();

    PremiumProvider<?> getPremiumProvider();

    <T> BeacmcAuth setPremiumProvider(PremiumProvider<T> premiumProvider);

    BeacmcAuth setProxy(Proxy proxy);

    BeacmcAuth setLibraryProvider(LibraryProvider libraryProvider);

    BeacmcAuth setServerLogger(ServerLogger serverLogger);

    LibraryProvider getLibraryProvider();

    ExecutorService getExecutorService();

    SongManager getSongManager();

    PlayerPositionTracker getPlayerPositionTracker();
}
