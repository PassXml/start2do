package org.start2do.script.util.impl.functions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum SystemConsole {
    INSTANCE;

    public void log(String message) {
        log.info(message);
    }

    public void log(Object... obs) {
        log.info("{}", obs);
    }

    public void log(String message, Object... obs) {
        log.info(message, obs);
    }

    public void info(String message, Object... obs) {
        log.info(message, obs);
    }

    public void info(String message) {
        log.info(message);
    }

    public void warn(String message, Object... obs) {
        log.warn(message, obs);
    }

    public void warn(Object... obs) {
        log.warn("{}", obs);
    }

    public void warn(String message) {
        log.warn(message);
    }


    public void error(String message, Object... obs) {
        log.error(message, obs);
    }

    public void error(Object... obs) {
        log.error("{}", obs);
    }

    public void error(String message) {
        log.error(message);
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void debug(String message, Object... obs) {
        log.debug(message, obs);
    }

    public void trace(String message) {
        log.trace(message);
    }

    public void trace(String message, Object... obs) {
        log.trace(message, obs);
    }

    public void trace(Object... obs) {
        log.trace("{}", obs);
    }
}
