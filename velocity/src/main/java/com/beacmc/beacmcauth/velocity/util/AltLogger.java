package com.beacmc.beacmcauth.velocity.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltLogger {

    private final Logger logger;

    private AltLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static AltLogger get(Class<?> clazz) {
        return new AltLogger(clazz);
    }

    public void info(String msg) {
        logger.info("[BeacmcAuth] {}", msg);
    }

    public void warn(String msg) {
        logger.warn("[BeacmcAuth] {}", msg);
    }

    public void error(String msg, Throwable t) {
        logger.error("[BeacmcAuth] {}", msg, t);
    }
}
