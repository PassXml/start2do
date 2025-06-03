package org.start2do.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.annotation.Annotation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Import(RedisConfiguration.class)
public class RedisOMConfig {

    @Bean("JacksonOM")
    @ConditionalOnMissingClass("JacksonOM")
    @ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
    public static ObjectMapper jacksonOM() {
        // 如果直接使用Jackson2JsonRedisSerializer 获取存储的对象则会变为LinkedHashMap,添加ObjectMapper可解决
        ObjectMapper objectMapper = jacksonOMFilter();
        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            protected boolean _isIgnorable(Annotated a) {
                for (String str : JacksonConstant.JPAAnnotation) {
                    try {
                        Class<?> aClass = Class.forName(str);
                        if (aClass.isAnnotation()) {
                            Annotation annotation = a.getAnnotation((Class<? extends Annotation>) aClass);
                            if (annotation != null) {
                                return true;
                            }
                        }
                    } catch (ClassNotFoundException e) {

                    }
                }
                return super._isIgnorable(a);
            }
        });
        return objectMapper;
    }

    @Bean("JacksonOM")
    @ConditionalOnMissingClass("ManyToOne")
    @ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
    public static ObjectMapper jacksonOMFilter() {
        // 如果直接使用Jackson2JsonRedisSerializer 获取存储的对象则会变为LinkedHashMap,添加ObjectMapper可解决
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Bean
    @ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory factory,
        @Qualifier("JacksonOM") ObjectMapper objectMapper, RedisConfiguration configuration) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        if (configuration.getKeyPrefix() != null && !configuration.getKeyPrefix().isEmpty()) {
            redisTemplate.setKeySerializer(new PrefixedKeySerializer(configuration.getKeyPrefix()));
        } else {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
        }
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
            Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }
}
