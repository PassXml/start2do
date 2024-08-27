package org.start2do.dto;

public class RateLimiterException extends RuntimeException {

    public RateLimiterException() {
        super("系统繁忙，请稍后再试");
    }

    public RateLimiterException(String message) {
        super(message);
    }
}
