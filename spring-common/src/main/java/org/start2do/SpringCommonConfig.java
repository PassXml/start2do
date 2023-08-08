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
@ConfigurationProperties(prefix = "start2do")
public class SpringCommonConfig {

    private ReplaceFilter replaceFilter;
    private ErrorTrace errorTrace;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class ReplaceFilter {

        private Boolean enable;
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class ErrorTrace {

        private String packageName;
    }
}
