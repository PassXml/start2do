package org.start2do.redis;

import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

public class PrefixedKeySerializer extends StringRedisSerializer {

    private final String prefix;

    public PrefixedKeySerializer(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public byte[] serialize(String key) {
        if (StringUtils.hasText(key)) {
            key = prefix + ":" + key;
        }
        return super.serialize(key);
    }
}
