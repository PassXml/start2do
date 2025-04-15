package org.start2do.script.impl.nashornl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemConsole {

    private final ByteArrayOutputStream outputStream;

    public SystemConsole(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void log(String message) {
        writeToStream(message);
        log.info(message);
    }

    public void log(Object... obs) {
        String message = formatMessage("{}", obs);
        writeToStream(message);
        log.info(message);
    }

    public void log(String message, Object... obs) {
        String formattedMessage = formatMessage(message, obs);
        writeToStream(formattedMessage);
        log.info(formattedMessage);
    }

    public void info(String message, Object... obs) {
        String formattedMessage = formatMessage(message, obs);
        writeToStream(formattedMessage);
        log.info(formattedMessage);
    }

    public void info(String message) {
        writeToStream(message);
        log.info(message);
    }

    public void warn(String message, Object... obs) {
        String formattedMessage = formatMessage(message, obs);
        writeToStream(formattedMessage);
        log.warn(formattedMessage);
    }

    public void warn(Object... obs) {
        String message = formatMessage("{}", obs);
        writeToStream(message);
        log.warn(message);
    }

    public void warn(String message) {
        writeToStream(message);
        log.warn(message);
    }

    public void error(String message, Object... obs) {
        String formattedMessage = formatMessage(message, obs);
        writeToStream(formattedMessage);
        log.error(formattedMessage);
    }

    public void error(Object... obs) {
        String message = formatMessage("{}", obs);
        writeToStream(message);
        log.error(message);
    }

    public void error(String message) {
        String formattedMessage = formatMessage(message);
        writeToStream(formattedMessage);
        log.error(formattedMessage);
    }

    public void debug(String message) {
        String formattedMessage = formatMessage(message);
        writeToStream(formattedMessage);
        log.debug(formattedMessage);
    }

    public void debug(String message, Object... obs) {
        String formattedMessage = formatMessage(message,obs);
        writeToStream(formattedMessage);
        log.debug(formattedMessage);
    }

    public void trace(String message) {
        String formattedMessage = formatMessage(message);
        writeToStream(formattedMessage);
        log.trace(formattedMessage);
    }

    public void trace(String message, Object... obs) {
        String formattedMessage = formatMessage(message, obs);
        writeToStream(formattedMessage);
        log.trace(formattedMessage);
    }

    public void trace(Object... obs) {
        String message = formatMessage("{}", obs);
        writeToStream(message);
        log.trace(message);
    }

    private void writeToStream(String message) {
        try {
            outputStream.write(message.getBytes());
            outputStream.write("\n".getBytes());
        } catch (IOException e) {
            // 忽略异常
        }
    }
    private String formatMessage(String message, Object... obs) {
        if (obs.length == 0) {
            return message;
        }
        // 直接使用 SLF4J 的 FormattingTuple 进行格式化
        org.slf4j.helpers.FormattingTuple tuple = org.slf4j.helpers.MessageFormatter.arrayFormat(message, obs);
        return tuple.getMessage();
    }
}
