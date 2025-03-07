package org.start2do.multi.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.util.StringUtils;
import org.start2do.redis.PrefixedKeySerializer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnClass(RedisTemplate.class)
public class MultiRedisAutoConfiguration {

    /**
     * 默认
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> template(RedisTemplateResolver resolver) {
        return resolver.resolve("default");
    }

    /**
     * 默认
     */
    @Bean
    @Primary
    public RedisConnectionFactory connectionFactory(RedisTemplateResolver resolver) {
        return resolver.getConnectionFactory("default");
    }

    @Bean
    @ConditionalOnBean(name = "JacksonOM")
    public RedisTemplateResolver redisTemplateResolver(RedisProperties properties,
        @Qualifier("JacksonOM") ObjectMapper objectMapper) {
        Map<String, RedisTemplate<String, Object>> templates = new HashMap<>();
        properties.getSources().forEach((name, config) -> {
            RedisTemplate<String, Object> template = createRedisTemplate(config);
            templates.put(name, template);
        });

        return new RedisTemplateResolver(templates);
    }

    @Bean
    @ConditionalOnMissingBean(name = "JacksonOM")
    public RedisTemplateResolver redisTemplateResolver(RedisProperties properties) {
        Map<String, RedisTemplate<String, Object>> templates = new HashMap<>();
        properties.getSources().forEach((name, config) -> {
            RedisTemplate<String, Object> template = createRedisTemplate(config);
            templates.put(name, template);
        });

        return new RedisTemplateResolver(templates);
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisProperties.RedisConnectionConfig config) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(createConnectionFactory(config));
        template.setKeySerializer(new PrefixedKeySerializer(config.getKeyPrefix()));
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new PrefixedKeySerializer(config.getKeyPrefix()));
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    private RedisConnectionFactory createConnectionFactory(RedisProperties.RedisConnectionConfig config) {
        LettuceConnectionFactory factory;

        switch (config.getMode()) {
            case SENTINEL:
                factory = createSentinelConnectionFactory(config);
                break;
            case CLUSTER:
                factory = createClusterConnectionFactory(config);
                break;
            default:
                factory = createStandaloneConnectionFactory(config);
        }

        factory.afterPropertiesSet();
        return factory;
    }

    private LettuceConnectionFactory createStandaloneConnectionFactory(RedisProperties.RedisConnectionConfig config) {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(config.getHost());
        standaloneConfig.setPort(config.getPort());
        standaloneConfig.setPassword(config.getPassword());
        standaloneConfig.setDatabase(config.getDatabase());

        return new LettuceConnectionFactory(standaloneConfig, getPoolConfig(config.getPool()));
    }

    private LettuceConnectionFactory createSentinelConnectionFactory(RedisProperties.RedisConnectionConfig config) {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.setMaster(config.getMaster());
        config.getNodes().forEach(node -> {
            String[] parts = node.split(":");
            sentinelConfig.addSentinel(new RedisNode(parts[0], Integer.parseInt(parts[1])));
        });

        return new LettuceConnectionFactory(sentinelConfig, getPoolConfig(config.getPool()));
    }

    private LettuceConnectionFactory createClusterConnectionFactory(RedisProperties.RedisConnectionConfig config) {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(config.getNodes());
        if (StringUtils.hasText(config.getPassword())) {
            clusterConfig.setPassword(config.getPassword());
        }

        return new LettuceConnectionFactory(clusterConfig, getPoolConfig(config.getPool()));
    }

    private LettucePoolingClientConfiguration getPoolConfig(RedisProperties.Pool pool) {
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        poolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
        return LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();
    }
}
