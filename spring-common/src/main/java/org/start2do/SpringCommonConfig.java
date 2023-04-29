package org.start2do;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2o")
@AutoConfiguration
public class SpringCommonConfig {

    private ReplaceFilter replaceFilter;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class ReplaceFilter {

        private Boolean enable;
    }
}
