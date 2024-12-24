package org.start2do.dto.config;

import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.business.cache")
public class CacheSettingConfig {

    private Boolean enable;
    private Type type;
    private Long entryTtl = 900l;
    private ChronoUnit timeUnit = ChronoUnit.SECONDS;
    private String redisCachePrefix = "Cache:";

    public enum Type {
        Redis, Ehcache, Caffeine, JCache;


    }
}
