package org.start2do.script.config;

import com.googlecode.aviator.FunctionMissing;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "start2do.script.av-setting")
@ConditionalOnProperty(prefix = "start2do.script", name = "enable", havingValue = "true")
public class AvSettingConfig {

    private boolean enable = false;
    private List<Class<? extends AbstractFunction>> functions;
    private Boolean enableJacksonFunction = false;
    private Boolean enableHikariDataSource = false;
    private Boolean enableOkhttpClient = false;
    private Boolean enableSystemFunctionMissing = false;
    private Class<? extends FunctionMissing> customFunctionMissingImpl;
    private List<Class<?>> importStaticFunction;

}
