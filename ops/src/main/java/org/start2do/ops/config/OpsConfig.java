package org.start2do.ops.config;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.ops")
public class OpsConfig {

    private boolean enable;
    private boolean enableSecurity=false;
    private String secret;
    private List<String> whitePath;
    private List<String> whiteSubfix;


}
