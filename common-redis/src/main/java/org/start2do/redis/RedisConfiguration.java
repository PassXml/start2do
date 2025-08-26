package org.start2do.redis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.util.redis")
public class RedisConfiguration {

    private boolean enable = false;
    /**
     * key统一前缀
     */
    private String keyPrefix;
}
