package org.start2do.util.spring;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.util")
public class UtilConfig {

    private boolean enable;
    private RedisConfig redis;


    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class RedisConfig {

        private String enable;
    }
}
