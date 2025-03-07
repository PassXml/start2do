package org.start2do.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.annotation.Annotation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class RedisOMConfig {

    @Bean("JacksonOM")
    @ConditionalOnMissingClass("JacksonOM")
    @ConditionalOnProperty(prefix = "start2do.util.redis", value = "enable", havingValue = "true")
    public static ObjectMapper jacksonOM() {
        // 如果直接使用Jackson2JsonRedisSerializer 获取存储的对象则会变为LinkedHashMap,添加ObjectMapper可解决
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            Class<?> aClass = Class.forName("javax.persistence.ManyToOne");
            objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                @Override
                protected boolean _isIgnorable(Annotated a) {
                    for (Class aClass : JacksonConstant.JPAAnnotation) {
                        Annotation annotation = a.getAnnotation(aClass);
                        if (annotation != null) {
                            return true;
                        }
                    }
                    return super._isIgnorable(a);
                }
            });
        } catch (ClassNotFoundException e) {
        }

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
}
