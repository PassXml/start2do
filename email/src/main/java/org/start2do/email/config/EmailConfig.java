package org.start2do.email.config;

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
@ConditionalOnProperty(prefix = "email", value = "enable", havingValue = "true")
@ConfigurationProperties(prefix = "email")
public class EmailConfig {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String from;

}
