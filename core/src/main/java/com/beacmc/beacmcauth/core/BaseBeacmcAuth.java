package com.beacmc.beacmcauth.core;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.premium.mojang.MojangAuthManager;
import com.beacmc.beacmcauth.api.auth.premium.mojang.PremiumChangerProvider;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.EmailConfig;
import com.beacmc.beacmcauth.api.config.MojangAuthConfig;
import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.api.config.social.DiscordConfig;
import com.beacmc.beacmcauth.api.config.social.TelegramConfig;
import com.beacmc.beacmcauth.api.config.social.VkontakteConfig;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.email.EmailManager;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.packet.position.PlayerPositionTracker;
import com.beacmc.beacmcauth.api.server.Proxy;
import com.beacmc.beacmcauth.api.social.SocialManager;
import com.beacmc.beacmcauth.api.song.SongManager;
import com.beacmc.beacmcauth.core.auth.BaseAuthManager;
import com.beacmc.beacmcauth.core.auth.mojang.BaseMojangAuthManager;
import com.beacmc.beacmcauth.core.command.BaseCommandManager;
import com.beacmc.beacmcauth.core.command.executor.*;
import com.beacmc.beacmcauth.core.config.BaseConfig;
import com.beacmc.beacmcauth.core.config.BaseEmailConfig;
import com.beacmc.beacmcauth.core.config.BaseMojangAuthConfig;
import com.beacmc.beacmcauth.core.config.loader.BaseConfigLoader;
import com.beacmc.beacmcauth.core.config.social.BaseDiscordConfig;
import com.beacmc.beacmcauth.core.config.social.BaseTelegramConfig;
import com.beacmc.beacmcauth.core.config.social.BaseVkontakteConfig;
import com.beacmc.beacmcauth.core.database.BaseDatabase;
import com.beacmc.beacmcauth.core.email.BaseEmailManager;
import com.beacmc.beacmcauth.core.library.Libraries;
import com.beacmc.beacmcauth.core.packet.BasePlayerPositionTracker;
import com.beacmc.beacmcauth.core.social.BaseSocialManager;
import com.beacmc.beacmcauth.core.social.types.discord.DiscordSocial;
import com.beacmc.beacmcauth.core.social.types.telegram.TelegramSocial;
import com.beacmc.beacmcauth.core.social.types.vkontakte.VkontakteSocial;
import com.beacmc.beacmcauth.core.song.BaseSongManager;
import lombok.Getter;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class BaseBeacmcAuth implements BeacmcAuth {

    private Proxy proxy;
    private ServerLogger serverLogger;
    private CommandManager commandManager;
    private PremiumChangerProvider<?> premiumProvider;
    private File dataFolder;
    private Database database;
    private ConfigLoader configLoader;
    private MessageProvider messageProvider;
    private LibraryProvider libraryProvider;
    private AuthManager authManager;
    private TelegramConfig telegramConfig;
    private Config config;
    private EmailConfig emailConfig;
    private MojangAuthConfig mojangAuthConfig;
    private DiscordConfig discordConfig;
    private VkontakteConfig vkontakteConfig;
    private SocialManager socialManager;
    private ExecutorService executorService;
    private SongManager songManager;
    private PlayerPositionTracker playerPositionTracker;
    private MojangAuthManager mojangAuthManager;
    private EmailManager emailManager;

    @Override
    public BeacmcAuth onEnable() {
        executorService = Executors.newFixedThreadPool(8);

        libraryProvider.loadLibrary(Libraries.JDA);

        configLoader = new BaseConfigLoader();
        reloadAllConfigurations();
        database = new BaseDatabase(this);
        database.init();
        mojangAuthManager = new BaseMojangAuthManager(this);
        authManager = new BaseAuthManager(this);
        emailManager = new BaseEmailManager(this);
        socialManager = new BaseSocialManager(this);
        if (telegramConfig.isEnabled()) {
            getLibraryProvider().loadLibrary(Libraries.KOTLIN);
            getLibraryProvider().loadLibrary(Libraries.OKIO);
            getLibraryProvider().loadLibrary(Libraries.OKHTTP);
            getLibraryProvider().loadLibrary(Libraries.TELEGRAM);
            socialManager.getSocials().add(new TelegramSocial(this));
        }

        if (discordConfig.isEnabled()) {
            socialManager.getSocials().add(new DiscordSocial(this));
        }
        if (vkontakteConfig.isEnabled()) {
            socialManager.getSocials().add(new VkontakteSocial(this));
        }

        playerPositionTracker = new BasePlayerPositionTracker(this);
        Path songs = getDataFolder().toPath().resolve("songs");
        if (!songs.toFile().exists()) {
            saveResource("songs/Panda.nbs");
        }

        songManager = new BaseSongManager(this);
        songManager.loadSongs(songs);

        commandManager = new BaseCommandManager();
        commandManager.register("register", new RegisterCommandExecutor(this));
        commandManager.register("login", new LoginCommandExecutor(this));
        commandManager.register("link", new LinkCommandExecutor(this));
        commandManager.register("changepassword", new ChangepasswordCommandExecutor(this));
        commandManager.register("auth", new AuthCommandExecutor(this));
        commandManager.register("premium", new PremiumCommandExecutor(this));
        commandManager.register("crack", new CrackCommandExecutor(this));
        commandManager.register("alts", new AltsCommandExecutor(this, authManager));
        commandManager.register("secret", new SecretCommandExecutor(this, authManager));
        commandManager.register("logout", new LogoutCommandExecutor(this, authManager));
        commandManager.register("email", new EmailCommandExecutor(this));
        return this;
    }

    @Override
    public BeacmcAuth onDisable() {
        commandManager = null;
        serverLogger = null;
        configLoader = null;
        return this;
    }

    @Override
    public void reloadAllConfigurations() {
        config = configLoader.load(new File(getDataFolder(), "config.yml"), BaseConfig.class, new BaseConfig())
                .setPlugin(this);
        emailConfig = configLoader.load(new File(getDataFolder(), "email.yml"), BaseEmailConfig.class, new BaseEmailConfig());
        telegramConfig = configLoader.load(new File(getDataFolder(), "telegram.yml"), BaseTelegramConfig.class, new BaseTelegramConfig());
        discordConfig = configLoader.load(new File(getDataFolder(), "discord.yml"), BaseDiscordConfig.class, new BaseDiscordConfig());
        vkontakteConfig = configLoader.load(new File(getDataFolder(), "vkontakte.yml"), BaseVkontakteConfig.class, new BaseVkontakteConfig());
        mojangAuthConfig = configLoader.load(new File(getDataFolder(), "license-auth.yml"), BaseMojangAuthConfig.class, new BaseMojangAuthConfig());
    }

    public <T> BeacmcAuth setPremiumProvider(PremiumChangerProvider<T> premiumChangerProvider) {
        this.premiumProvider = premiumChangerProvider;
        return this;
    }

    @Override
    public BeacmcAuth setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    @Override
    public BeacmcAuth setLibraryProvider(LibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
        return this;
    }


    @Override
    public BeacmcAuth setMessageProvider(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
        return this;
    }

    @Override
    public BeacmcAuth setServerLogger(ServerLogger serverLogger) {
        this.serverLogger = serverLogger;
        return this;
    }

    @Override
    public BeacmcAuth setDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
        return this;
    }

    @Override
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

    @Override
    public InputStream getResource(String file) {
        return getClass().getClassLoader().getResourceAsStream(file);
    }
}
