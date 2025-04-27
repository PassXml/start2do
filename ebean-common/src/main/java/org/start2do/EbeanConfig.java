package org.start2do;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "start2do.ebean")
public class EbeanConfig {

    private boolean migration = false;
    private boolean multipleDataSources = false;
    private boolean enable = false;
    private boolean enableSettingService = false;
    /**
     * 是否启用hook
     */
    private boolean enableHooks = false;
}
