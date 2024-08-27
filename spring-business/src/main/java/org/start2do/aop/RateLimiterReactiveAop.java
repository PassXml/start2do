package org.start2do.aop;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.start2do.BusinessConfig;
import org.start2do.dto.RateLimiterException;
import org.start2do.util.StringUtils;
import org.start2do.util.spring.RedisCacheUtil;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.rate-limit", value = "enable", havingValue = "true")
public class RateLimiterReactiveAop {

    private final BusinessConfig businessConfig;
    private RedisScript<List> script = null;

    @PostConstruct
    public void init() {
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(businessConfig.getRateLimit().getLuaScript());
        redisScript.setResultType(List.class);
        script = redisScript;
    }

    protected List<String> getKey(String id) {
        String prefix = "rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    @Around("@annotation(setting)")
    public Object around(ProceedingJoinPoint point, RateLimitSetting setting) throws Throwable {
        List<String> keys = null;
        boolean found = false;
        if (StringUtils.isEmpty(setting.id())) {
            for (Object arg : point.getArgs()) {
                if (IRetaLimitGetterKey.class.isAssignableFrom(arg.getClass())) {
                    keys = getKey(((IRetaLimitGetterKey) arg).getPrefix());
                    found = true;
                    break;
                }
            }
            if (!found) {
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keys = getKey(
                    method.getDeclaringClass().getName() + "." + method.getName() + "." + method.getParameterCount());
            }
        } else {
            keys = getKey(setting.id());
        }
        if (keys != null) {
            final long startTime = java.lang.System.nanoTime();
            List<Long> longs = RedisCacheUtil.executorScript(script, keys, setting.rate(), setting.capacity(),
                Instant.now().getEpochSecond(), setting.requested());
            do {
                if (!longs.isEmpty()) {
                    if (longs.get(0) == 1L) {
                        break;
                    }
                    if (!setting.await()) {
                        throw new RateLimiterException();
                    } else {
                        boolean waited = waitForPermission(startTime, Duration.ofMillis(setting.waitMs()).toNanos());
                        if (Thread.currentThread().isInterrupted()) {
                            throw new RateLimiterException("获取令牌异常");
                        }
                        if (!waited) {
                            throw new RateLimiterException("等待令牌超时");
                        }
                    }
                }
            } while (false);
        }
        return point.proceed();
    }

    interface IRetaLimitGetterKey {

        String getPrefix();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RateLimitSetting {

        @Min(1) int requested() default 1;

        @Min(1) int capacity() default 10000;

        @Min(1) int rate() default 200;

        String id() default "";

        boolean await() default false;

        /**
         * 等待1秒
         */
        long waitMs() default 1000;
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

}
