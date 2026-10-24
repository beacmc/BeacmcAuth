package com.beacmc.beacmcauth.api;

import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.PremiumChangerProvider;
import com.beacmc.beacmcauth.api.auth.protection.ProtectionManager;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.ProtectionConfig;
import com.beacmc.beacmcauth.api.config.social.DiscordConfig;
import com.beacmc.beacmcauth.api.config.social.TelegramConfig;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.dialog.DialogManager;
import com.beacmc.beacmcauth.api.event.EventManager;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.packet.position.PlayerPositionTracker;
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

    DialogManager getDialogManager();

    Database getDatabase();

    EventManager getEventManager();

    AuthManager getAuthManager();

    void reloadAllConfigurations();

    void reloadDialogs();

    MessageProvider getMessageProvider();

    BeacmcAuth setMessageProvider(MessageProvider messageProvider);

    Proxy getProxy();

    TelegramConfig getTelegramConfig();

    File getDataFolder();

    DiscordConfig getDiscordConfig();

    void saveResource(String file);

    SocialManager getSocialManager();

    InputStream getResource(String file);

    BeacmcAuth setDataFolder(File file);

    ServerLogger getServerLogger();

    CommandManager getCommandManager();

    PremiumChangerProvider<?> getPremiumProvider();

    <T> BeacmcAuth setPremiumProvider(PremiumChangerProvider<T> premiumChangerProvider);

    BeacmcAuth setProxy(Proxy proxy);

    BeacmcAuth setLibraryProvider(LibraryProvider libraryProvider);

    BeacmcAuth setServerLogger(ServerLogger serverLogger);

    LibraryProvider getLibraryProvider();

    ExecutorService getExecutorService();

    SongManager getSongManager();

    PlayerPositionTracker getPlayerPositionTracker();

    ProtectionManager getProtectionManager();

    ProtectionConfig getProtectionConfig();
}
