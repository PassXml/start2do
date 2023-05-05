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

    private Boolean enable;
    private SysLogConfig sysLog;

    private String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
    private String datePattern = "yyyy-MM-dd";

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class SysLogConfig {

        private boolean enable;

    }
}
