package org.start2do.util.spring;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ScanOptions.ScanOptionsBuilder;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
public class RedisCacheUtil implements CommandLineRunner {

    public static RedisTemplate<String, Object> getRedisTemplate() {
        return redisCacheUtil.redisTemplate;
    }

    private final RedisTemplate<String, Object> redisTemplate;
    private static RedisMessageListenerContainer container = new RedisMessageListenerContainer();

    private static RedisCacheUtil redisCacheUtil;

    public static List<String> scan(RedisTemplate redisTemplate, String query, Integer maxCount) {
        if (redisTemplate == null) {
            return new ArrayList<>();
        }
        Set<String> execute = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            ScanOptionsBuilder builder = ScanOptions.scanOptions().match("*" + query + "*");
            if (maxCount != null) {
                builder.count(maxCount);
            }
            try (Cursor<byte[]> scan = connection.scan(builder.build())) {
                Set<String> keys = new HashSet<>();
                while (scan.hasNext()) {
                    byte[] next = scan.next();
                    keys.add(new String(next));
                }
                return keys;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return new HashSet<>();
            }
        });
        return new ArrayList<>(execute);
    }

    public static List<String> scan(String key) {
        if (RedisCacheUtil.redisCacheUtil == null) {
            return new ArrayList<>();
        }
        try {
            return scan(RedisCacheUtil.redisCacheUtil.redisTemplate, key, 1000);
        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    public static List<String> scan(String key, Integer maxCount) {
        if (RedisCacheUtil.redisCacheUtil == null) {
            return new ArrayList<>();
        }
        try {
            return scan(RedisCacheUtil.redisCacheUtil.redisTemplate, key, maxCount);
        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    public static void zset(String key, Object id, int size) {
        redisCacheUtil.redisTemplate.opsForZSet().add(key, id, size);
    }

    public static Double zsetSore(String key, Object value) {
        return redisCacheUtil.redisTemplate.opsForZSet().score(key, value);
    }

    public static Set<Object> zsetRange(String key, int start, int end) {
        return redisCacheUtil.redisTemplate.opsForZSet().reverseRange(key, start, end);
    }


    public static <T> T get(String key) {
        Object o = redisCacheUtil.redisTemplate.opsForValue().get(key);
        if (o != null) {
            return (T) o;
        }
        return null;
    }

    public static void remove(String key) {
        redisCacheUtil.redisTemplate.delete(key);
    }

    public static <T> T get(String key, Supplier<T> function) {
        return get(key, function, 30, TimeUnit.MINUTES);
    }

    public static Boolean hasKey(String key) {
        return redisCacheUtil.redisTemplate.hasKey(key);
    }

    public static <T> T get(String key, Supplier<T> function, long time, TimeUnit timeUnit) {
        Object o = redisCacheUtil.redisTemplate.opsForValue().get(key);
        if (o != null) {
            return (T) o;
        } else {
            T result = function.get();
            if (result != null) {
                redisCacheUtil.redisTemplate.opsForValue().set(key, result, time, timeUnit);
            }
            return result;
        }
    }

    public static <T> Mono<T> tryLockReactive(String key, Integer time, TimeUnit timeUnit, Runnable run,
        RuntimeException exception) {
        if (Boolean.TRUE.equals(redisCacheUtil.redisTemplate.hasKey(key))) {
            if (exception != null) {
                Mono.error(exception);
            }
        }
        return Mono.create(sink -> {
            set(key, 1, time, timeUnit);
            try {
                run.run();
                sink.success();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sink.error(e);
            } finally {
                remove(key);
            }
        });
    }

    public static <T> T tryLock(String key, Integer time, TimeUnit timeUnit, Supplier<T> run,
        RuntimeException exception) {
        if (Boolean.TRUE.equals(redisCacheUtil.redisTemplate.hasKey(key))) {
            if (exception != null) {
                throw exception;
            }
        }
        set(key, 1, time, timeUnit);
        try {
            return run.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            remove(key);
        }
    }

    public static void tryLock(String key, Integer time, TimeUnit timeUnit, Runnable run, RuntimeException exception) {
        if (Boolean.TRUE.equals(redisCacheUtil.redisTemplate.hasKey(key))) {
            if (exception != null) {
                throw exception;
            }
        }
        set(key, 1, time, timeUnit);
        try {
            run.run();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            remove(key);
        }
    }

    public static void tryLock(String key, Runnable run, RuntimeException exception) {
        tryLock(key, 5, TimeUnit.SECONDS, run, exception);
    }

    public static void set(String key, Object obj) {
        redisCacheUtil.redisTemplate.opsForValue().set(key, obj);
    }

    public static void set(String key, Object obj, long time, TimeUnit timeUnit) {
        if (time < 0) {
            redisCacheUtil.redisTemplate.opsForValue().set(key, obj);
        } else {
            redisCacheUtil.redisTemplate.opsForValue().set(key, obj, time, timeUnit);
        }
    }

    public static Boolean expire(String key, long time, TimeUnit timeUnit) {
        return redisCacheUtil.redisTemplate.expire(key, time, timeUnit);
    }

    private static void incrementPre(String key, Integer defaultValue) {
        if (redisCacheUtil.redisTemplate.hasKey(key)) {
            return;
        }
        if (defaultValue == null) {
            defaultValue = 0;
        }
        redisCacheUtil.redisTemplate.opsForValue().set(key, defaultValue);
    }

    public static Long increment(String key) {
        return increment(key, 1, 0);
    }

    public static Long increment(String key, long i, Integer defaultValue) {
        incrementPre(key, defaultValue);
        return redisCacheUtil.redisTemplate.opsForValue().increment(key, i);
    }

    public static Long increment(String key, long i) {
        return increment(key, i, 0);
    }

    public static Long convertAndSend(String key, Object obj) {
        return redisCacheUtil.redisTemplate.convertAndSend(key, obj);
    }

    public static void addMessageListener(MessageListener listener, Topic topic) {
        container.addMessageListener(listener, topic);
    }

    public static void addMessageListener(MessageListener listener, List<Topic> topic) {
        container.addMessageListener(listener, topic);
    }

    @PostConstruct
    public void init() {
        RedisCacheUtil.redisCacheUtil = this;
        RedisConnectionFactory connectionFactory = this.redisTemplate.getConnectionFactory();
        if (connectionFactory instanceof LettuceConnectionFactory) {
            ((LettuceConnectionFactory) connectionFactory).setValidateConnection(true);
            ((LettuceConnectionFactory) connectionFactory).afterPropertiesSet();
        }
        container.setConnectionFactory(connectionFactory);
    }

    /**
     * 用于在一个 Redis 会话中执行多个操作，这些操作会被打包成一个事务或管道（pipeline），从而提高性能。适合需要在一个会话中执行多个操作的场景。
     */
    public <T> List<Object> executePipelined(SessionCallback<T> session) {
        return redisCacheUtil.redisTemplate.executePipelined(session);
    }

    /**
     * 用于在一个 Redis 连接中执行自定义的 Redis 操作。适合需要直接操作 Redis 连接的场景。
     */
    public <T> List<Object> executePipelined(RedisCallback<T> callback) {
        return redisCacheUtil.redisTemplate.executePipelined(callback);
    }

    public Object mget(Collection<String> keys){
        return  redisCacheUtil.redisTemplate.opsForValue().multiGet(keys);
    }

    @Override
    public void run(String... args) throws Exception {
        init();
    }

    public static <T> T executorScript(RedisScript<T> script, List<String> keys, Object... args) {
        return redisCacheUtil.redisTemplate.<T>execute(script, keys, args);
    }

}
