package com.beacmc.beacmcauth.velocity.logger;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import org.slf4j.Logger;

public class VelocityServerLogger implements ServerLogger {

    private final Logger logger;
    private final BeacmcAuth plugin;

    public VelocityServerLogger(BeacmcAuth plugin) {
        this.logger = VelocityBeacmcAuth.getInstance().getVelocityLogger();
        this.plugin = plugin;
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void debug(String message) {
        final Config config = plugin.getConfig();
        if (config.isDebugEnabled()) logger.error("[DEBUG] " + message);
    }
}
