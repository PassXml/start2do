package org.start2do.config;

import java.util.HashSet;
import java.util.Set;
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
@ConfigurationProperties(prefix = "start2do.permission")
public class PermissionConfig {

    private boolean enable = false;
    /**
     * 默认放行用户组
     */
    private Set<String> ignoreDefaultRole = new HashSet<>(1);
}
