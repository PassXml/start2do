package org.start2do.script;


import com.googlecode.aviator.runtime.function.AbstractFunction;
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
@ConfigurationProperties(prefix = "start2do.script")
public class ScriptRunnerConfiguration {

    private boolean enable = false;
    private Type defaultRunner = Type.JS;

    private JsSetting jsSetting;
    private AvSetting avSetting;

    public enum Type {
        JS, Aviator,Groovy
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class JsSetting {

        private boolean enable = false;
        private String globalScript;
        private List<Class<?>> clazzList;
        private Integer maxSize;
        private Duration expireAfterAccess;
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class AvSetting {
        private boolean enable = false;
        private List<Class<? extends AbstractFunction>> functions;
        private Boolean enableJacksonFunction = false;
        private Boolean enableHikariDataSource = false;
        private Boolean enableOkhttpClient = false;
    }

}
