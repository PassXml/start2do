package org.start2do.script.config;

import java.time.Duration;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor

@ConfigurationProperties(prefix = "start2do.script.nashorn")
public class NashornJsConfig {

    private boolean enable = false;
    private int maxPoolSize = 20;
    private String globalScript = "";
    private long maxCPUTime;
    private long maxMemory;
    private boolean enableDB = false;
    private boolean enableJsonOperate = false;
    private boolean enableHTTP = false;
    private Set<String> whiteList;
    private Duration expireAfterAccess;
}
