package org.start2do.script.config;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.script.graaljs")
public class GraalJsConfig {

    private boolean enable = false;
    private String globalScript;
    private List<Class<?>> clazzList;
    private Integer maxSize;
    private Duration expireAfterAccess;
}
