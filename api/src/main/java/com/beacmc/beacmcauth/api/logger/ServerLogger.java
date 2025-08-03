package com.beacmc.beacmcauth.api.logger;

public interface ServerLogger {

    void log(String message);

    void warn(String message);

    void error(String message);

    void debug(String message);
}
