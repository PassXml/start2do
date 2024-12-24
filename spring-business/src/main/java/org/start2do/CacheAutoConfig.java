package org.start2do;


import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.start2do.dto.BusinessException;
import org.start2do.dto.config.CacheSettingConfig;

@Import(CacheSettingConfig.class)
@ConditionalOnProperty(prefix = "start2do.business.cache", name = "enable", matchIfMissing = true)
public class CacheAutoConfig {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(CacheSettingConfig config) {
        switch (config.getType()) {
            case JCache: {
                return new JCacheCacheManager();
            }
            case Caffeine: {
                CaffeineCacheManager manager = new CaffeineCacheManager();
                Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
                caffeine.expireAfterAccess(Duration.of(config.getEntryTtl(), config.getTimeUnit()));
                manager.setCaffeine(caffeine);
                return manager;
            }
            case Ehcache: {
                return new EhCacheCacheManager();
            }
        }
        throw new BusinessException("缓存配置无效");
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheManager cacheManager(CacheSettingConfig config, RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration.entryTtl(Duration.of(config.getEntryTtl(), config.getTimeUnit()));
        configuration.computePrefixWith(CacheKeyPrefix.prefixed(config.getRedisCachePrefix()));
        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(configuration).build();
    }
}
