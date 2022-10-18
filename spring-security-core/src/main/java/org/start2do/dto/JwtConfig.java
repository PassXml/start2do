package org.start2do.dto;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.start2do.util.JwtTokenUtil;

@Slf4j
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "jwt", name = "enable", havingValue = "true")
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;

    @PostConstruct
    public void init() {
        log.info(this.secret);
        JwtTokenUtil.SECRET = secret;
    }
}
