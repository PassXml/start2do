package org.start2do.script;


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
@ConfigurationProperties(prefix = "start2do.script")
@ConditionalOnProperty(prefix = "start2do.script", name = "enable", havingValue = "true")
public class ScriptRunnerConfiguration {

    private boolean enable = false;
    private Type defaultRunner = Type.Nashorn;


    public enum Type {
        GraalJS, Nashorn, Aviator, Groovy
    }


}
