package org.start2do;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "start2do.business")
@NoArgsConstructor
public class BusinessConfig {

    private SysLogConfig sysLog;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class SysLogConfig {

        private boolean enable;

    }
}
