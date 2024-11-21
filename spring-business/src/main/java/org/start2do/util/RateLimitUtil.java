package org.start2do.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import lombok.Getter;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.start2do.BusinessConfig;
import org.start2do.dto.RateLimiterException;
import org.start2do.util.spring.RedisCacheUtil;

public class RateLimitUtil {

    @Getter
    private RedisScript<List> script = null;

    public RateLimitUtil(BusinessConfig businessConfig) {
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(businessConfig.getRateLimit().getLuaScript());
        redisScript.setResultType(List.class);
        this.script = redisScript;
    }

    private long currentNanoTime(final long nanoTimeStart) {
        return java.lang.System.nanoTime() - nanoTimeStart;
    }

    private boolean waitForPermission(final long startTime, final long nanosToWait) {
        long deadline = currentNanoTime(startTime) + nanosToWait;
        boolean wasInterrupted = false;
        while (currentNanoTime(startTime) < deadline && !wasInterrupted) {
            long sleepBlockDuration = deadline - currentNanoTime(startTime);
            LockSupport.parkNanos(sleepBlockDuration);
            wasInterrupted = Thread.interrupted();
        }
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        }
        return !wasInterrupted;
    }

    public List<String> getKey(String id) {
        String prefix = "rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    public void getToken(List<String> keys, boolean isWaitToken, int requested, int capacity, int rate, long waitMs,
        long maxWaitMs, String notGetTokenMsg, String maxWaitErrorMsg) {
        final long startTime = java.lang.System.nanoTime();
        List<Long> longs = RedisCacheUtil.executorScript(script, keys, rate, capacity, Instant.now().getEpochSecond(),
            requested);
        long endWaitMs = Duration.ofMillis(maxWaitMs).toNanos();
        if (!longs.isEmpty()) {
            do {
                if (longs.get(0) == 1L) {
                    break;
                }
                if (!isWaitToken) {
                    throw new RateLimiterException();
                } else {
                    boolean waited = waitForPermission(startTime, Duration.ofMillis(waitMs).toNanos());
                    if (Thread.currentThread().isInterrupted()) {
                        throw new RateLimiterException(notGetTokenMsg);
                    }
                    if (!waited) {
                        throw new RateLimiterException(maxWaitErrorMsg);
                    }
                }
                if ((System.nanoTime() - startTime) > endWaitMs) {
                    throw new RateLimiterException(maxWaitErrorMsg);
                }
            } while (true);
        }
    }
}
