package org.start2do.multi.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
public class RedisProperties {

    private boolean enableMultiJacksonOM = false;
    private Map<String, RedisConnectionConfig> sources = new HashMap<>();

    @Data
    public static class RedisConnectionConfig {

        private RedisMode mode = RedisMode.STANDALONE;
        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int database = 0;
        // 哨兵配置
        private String master;
        private List<String> nodes = new ArrayList<>();
        // 连接池配置
        private Pool pool = new Pool();
        private String keyPrefix = "";
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Pool {

        private int maxActive = 8;
        private int maxIdle = 8;
        private int minIdle = 0;
        private Duration maxWait = Duration.ofMillis(-1);
    }

    public enum RedisMode {
        STANDALONE, SENTINEL, CLUSTER
    }

    // getter and setter
}
