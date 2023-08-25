package org.start2do.util.spring;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
public class RedisCacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private static RedisCacheUtil redisCacheUtil;

    @PostConstruct
    public void init() {
        RedisCacheUtil.redisCacheUtil = this;
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

    private static void incrementPre(String key) {
        if (redisCacheUtil.redisTemplate.hasKey(key)) {
            return;
        }
        redisCacheUtil.redisTemplate.opsForValue().set(key, 0);
    }

    public static void increment(String key) {
        incrementPre(key);
        redisCacheUtil.redisTemplate.opsForValue().increment(key);
    }

    public static void increment(String key, long i) {
        incrementPre(key);
        redisCacheUtil.redisTemplate.opsForValue().increment(key, i);
    }

    public static void buildByJRedis(String host, Integer port) {
        if (port == null) {
            port = 6379;
        }
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(host);
        jedisConnectionFactory.setPort(port);
        jedisConnectionFactory.afterPropertiesSet();
        UtilAutoConfig config = new UtilAutoConfig();
        RedisTemplate<String, Object> template = config.objectRedisTemplate(jedisConnectionFactory);
        template.afterPropertiesSet();
        RedisCacheUtil cacheUtil = new RedisCacheUtil(template);
        RedisCacheUtil.redisCacheUtil = cacheUtil;
    }


}
