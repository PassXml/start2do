package org.start2do.util.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.start2do.redis.PrefixedKeySerializer;
import org.start2do.util.spring.UtilConfig.RedisConfig;


@Slf4j
@Import({UtilConfig.class, LogAopConfig.class})
@ConditionalOnProperty(prefix = "start2do.util", value = "enable", havingValue = "true")
public class UtilAutoConfig {

    @Bean
    @ConditionalOnMissingBean(ILogConfigBean.class)
    public ILogConfigBean logConfigBean() {
        return () -> new SimpleFilterProvider().addFilter("password_filter",
            SimpleBeanPropertyFilter.serializeAllExcept("password"));
    }

    @Bean
    @ConditionalOnMissingBean(LogAop.JSON.class)
    public LogAop.JSON json(LogAopConfig logAopConfig, ObjectMapper objectMapper) {
        return object -> {
            try {
                String json = objectMapper.writeValueAsString(object);
                int maxLength = logAopConfig.getMaxLogLength() != null ? logAopConfig.getMaxLogLength() : 2000;
                if (json.length() > maxLength) {
                    return json.substring(0, maxLength) + "...";
                }
                return json;
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                return e.getMessage();
            }
        };
    }


    @Bean
    @ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory factory,
        @Qualifier("JacksonOM") ObjectMapper objectMapper, UtilConfig utilConfig) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        RedisConfig redis = utilConfig.getRedis();
        if (redis == null) {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
        } else {
            redisTemplate.setKeySerializer(new PrefixedKeySerializer(redis.getKeyPrefix()));
        }
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
            objectMapper, Object.class);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }
}
