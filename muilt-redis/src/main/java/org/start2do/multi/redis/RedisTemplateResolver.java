package org.start2do.multi.redis;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class RedisTemplateResolver {

    private final Map<String, RedisTemplate<String, Object>> templates;

    public RedisTemplateResolver(Map<String, RedisTemplate<String, Object>> templates) {
        this.templates = templates;
    }

    public RedisTemplate<String, Object> resolve(String source) {
        RedisTemplate<String, Object> template = templates.get(source);
        if (template == null) {
            throw new IllegalArgumentException("Redis source not found: " + source);
        }
        return template;
    }

    public RedisConnectionFactory getConnectionFactory(String source) {
        RedisTemplate<String, Object> template = resolve(source);
        if (template == null) {
            throw new IllegalArgumentException("Redis source not found: " + source);
        }
        return template.getConnectionFactory();
    }
}
