package com.beacmc.beacmcauth.bungeecord.logger;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;

import java.util.logging.Logger;

public class BungeeServerLogger implements ServerLogger {

    private final Logger logger;
    private final BeacmcAuth plugin;

    public BungeeServerLogger(BeacmcAuth plugin) {
        this.logger = BungeeBeacmcAuth.getInstance().getLogger();
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }

    @Override
    public void debug(String message) {
        final Config config = plugin.getConfig();
        if (config.isDebugEnabled()) logger.severe("[DEBUG] " + message);
    }
}
