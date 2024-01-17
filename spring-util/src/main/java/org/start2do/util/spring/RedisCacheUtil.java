package org.start2do.util.spring;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
public class RedisCacheUtil {

    public static RedisTemplate<String, Object> getRedisTemplate() {
        return redisCacheUtil.redisTemplate;
    }

    private final RedisTemplate<String, Object> redisTemplate;
    private static RedisMessageListenerContainer container = new RedisMessageListenerContainer();

    private static RedisCacheUtil redisCacheUtil;

    public static List<String> scan(RedisTemplate redisTemplate, String query) {
        if (redisTemplate == null) {
            return new ArrayList<>();
        }
        Set<String> resultKeys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            ScanOptions scanOptions = ScanOptions.scanOptions().match("*" + query + "*").count(1000).build();
            Cursor<byte[]> scan = connection.scan(scanOptions);
            Set<String> keys = new HashSet<>();
            while (scan.hasNext()) {
                byte[] next = scan.next();
                keys.add(new String(next));
            }
            return keys;
        });

        return new ArrayList<>(resultKeys);
    }

    public static List<String> scan(String key) {
        if (RedisCacheUtil.redisCacheUtil == null) {
            return new ArrayList<>();
        }
        return scan(RedisCacheUtil.redisCacheUtil.redisTemplate, key);
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


    @PostConstruct
    public void init() {
        RedisCacheUtil.redisCacheUtil = this;
        container.setConnectionFactory(this.redisTemplate.getConnectionFactory());
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

    public static <T> Optional<T> tryLock(String key, Supplier<T> o) {
        if (redisCacheUtil.redisTemplate.hasKey(key)) {
            return Optional.empty();
        }
        set(key, 1, 5, TimeUnit.SECONDS);
        T t = null;
        try {
            t = o.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            remove(key);
        }
        return Optional.ofNullable(t);

    }


    public static void set(String key, Object obj) {
        redisCacheUtil.redisTemplate.opsForValue().set(key, obj);
    }

    public static void set(String key, Object obj, long time, TimeUnit timeUnit) {
        redisCacheUtil.redisTemplate.opsForValue().set(key, obj, time, timeUnit);
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


}
