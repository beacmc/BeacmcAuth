package com.beacmc.beacmcauth.bungeecord;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.beacmc.beacmcauth.bungeecord.auth.BungeePremiumChangerProvider;
import com.beacmc.beacmcauth.bungeecord.library.BungeeLibraryProvider;
import com.beacmc.beacmcauth.bungeecord.logger.BungeeServerLogger;
import com.beacmc.beacmcauth.bungeecord.message.BungeeMessageProvider;
import com.beacmc.beacmcauth.bungeecord.server.BungeeProxy;
import com.beacmc.beacmcauth.bungeecord.server.command.*;
import com.beacmc.beacmcauth.bungeecord.server.listener.AuthListener;
import com.beacmc.beacmcauth.bungeecord.server.listener.VkontakteListener;
import com.beacmc.beacmcauth.core.BaseBeacmcAuth;
import com.beacmc.beacmcauth.core.social.types.vkontakte.VkontakteSocial;
import com.ubivashka.vk.bungee.BungeeVkApiPlugin;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

@Getter
public final class BungeeBeacmcAuth extends Plugin {

    @Getter
    private static BungeeBeacmcAuth instance;
    private BeacmcAuth beacmcAuth;
    
    @Override
    public void onEnable() {
        instance = this;
        beacmcAuth = new BaseBeacmcAuth();
        beacmcAuth.setDataFolder(getDataFolder())
                .setProxy(new BungeeProxy(getProxy()))
                .setLibraryProvider(new BungeeLibraryProvider(this))
                .setMessageProvider(new BungeeMessageProvider())
                .setServerLogger(new BungeeServerLogger(beacmcAuth))
                .setPremiumProvider(new BungeePremiumChangerProvider());

        beacmcAuth.onEnable();

        Config config = beacmcAuth.getConfig();
        Metrics metrics = new Metrics(this, 23866);
        metrics.addCustomChart(new SimplePie("Database type",
                () -> config.getDatabaseSettings().getType().name()));
        metrics.addCustomChart(new SimplePie("Auth servers count",
                () -> String.valueOf(config.getAuthServers().size())));
        metrics.addCustomChart(new SingleLineChart("Registered players",
                () -> Math.toIntExact(beacmcAuth.getDatabase().getProtectedPlayerDao().countOf())));

        VkontakteSocial vkSocial = (VkontakteSocial) beacmcAuth.getSocialManager().getSocialByType(SocialType.VKONTAKTE);
        if (this.getProxy().getPluginManager().getPlugin("VK-API") != null && vkSocial != null) {
            vkSocial.setup(BungeeVkApiPlugin.getInstance());
            this.getProxy().getPluginManager().registerListener(this, new VkontakteListener(beacmcAuth));
        }

        this.getProxy().getPluginManager().registerListener(this, new AuthListener(beacmcAuth));
        initCommands();
    }

    private void initCommands() {
        this.getProxy().getPluginManager().registerCommand(this, new LoginCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new RegisterCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new AuthCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new ChangepasswordCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new LinkCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new PremiumCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new CrackCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new AltsCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new SecretCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new EmailCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new LogoutCommand(beacmcAuth));
    }

    @Override
    public void onDisable() {
        if (beacmcAuth != null) beacmcAuth.onDisable();
        instance = null;
    }
}
