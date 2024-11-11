package com.beacmc.beacmcauth;

import com.beacmc.beacmcauth.auth.AuthManager;
import com.beacmc.beacmcauth.command.*;
import com.beacmc.beacmcauth.config.ConfigLoader;
import com.beacmc.beacmcauth.config.impl.BaseConfig;
import com.beacmc.beacmcauth.config.impl.DiscordConfig;
import com.beacmc.beacmcauth.config.impl.TelegramConfig;
import com.beacmc.beacmcauth.database.Database;
import com.beacmc.beacmcauth.discord.DiscordProvider;
import com.beacmc.beacmcauth.lib.Libraries;
import com.beacmc.beacmcauth.lib.LibraryLoader;
import com.beacmc.beacmcauth.listener.AuthListener;
import com.beacmc.beacmcauth.telegram.TelegramProvider;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.io.*;

public final class BeacmcAuth extends Plugin {

    private static BeacmcAuth instance;
    private static ConfigLoader configLoader;
    private static BaseConfig config;
    private static DiscordConfig discordConfig;
    private static DiscordProvider discordProvider;
    private static LibraryLoader libraryLoader;
    private static Database database;
    private static AuthManager authManager;
    private static TelegramConfig telegramConfig;
    private static TelegramProvider telegramProvider;

    @Override
    public void onEnable() {
        instance = this;
        libraryLoader = new LibraryLoader();
        libraryLoader.loadLibrary(Libraries.ORMLITE);
        reloadConfigs();
        authManager = new AuthManager();
        database = new Database();
        database.connect();
        initSocials();
        Metrics metrics = new Metrics(this, 23866);
        this.getProxy().getPluginManager().registerListener(this, new AuthListener());
        initCommands();
    }

    private void initCommands() {
        this.getProxy().getPluginManager().registerCommand(this, new RegisterCommand());
        this.getProxy().getPluginManager().registerCommand(this, new AuthCommand());
        this.getProxy().getPluginManager().registerCommand(this, new LoginCommand());
        this.getProxy().getPluginManager().registerCommand(this, new LinkCommand());
        this.getProxy().getPluginManager().registerCommand(this, new ChangepasswordCommand());
    }

    private void initSocials() {
        if (telegramConfig.isEnabled()) {
            libraryLoader.loadLibrary(Libraries.TELEGRAM);
            telegramProvider = new TelegramProvider();
        }
        if (discordConfig.isEnabled()) {
            libraryLoader.loadLibrary(Libraries.JDA);
            discordProvider = new DiscordProvider();
        }
    }

    public void reloadConfigs() {
        saveResource("config.yml");
        saveResource("discord.yml");
        saveResource("telegram.yml");
        configLoader = new ConfigLoader();
        config = new BaseConfig(configLoader);
        discordConfig = new DiscordConfig(configLoader);
        telegramConfig = new TelegramConfig(configLoader);
    }

    public void saveResource(String resourcePath) {
        if (resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        try {
            if (!outFile.exists()) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            System.out.println("Could not save " + outFile.getName() + " to " + outFile);
        }
    }

    public static AuthManager getAuthManager() {
        return authManager;
    }

    public static Database getDatabase() {
        return database;
    }

    public static LibraryLoader getLibraryLoader() {
        return libraryLoader;
    }

    public InputStream getResource(String file) {
        return getClass().getClassLoader().getResourceAsStream(file);
    }

    public static ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public static BaseConfig getConfig() {
        return config;
    }

    public static DiscordProvider getDiscordProvider() {
        return discordProvider;
    }

    public static DiscordConfig getDiscordConfig() {
        return discordConfig;
    }

    public static TelegramProvider getTelegramProvider() {
        return telegramProvider;
    }

    public static TelegramConfig getTelegramConfig() {
        return telegramConfig;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static BeacmcAuth getInstance() {
        return instance;
    }
}
