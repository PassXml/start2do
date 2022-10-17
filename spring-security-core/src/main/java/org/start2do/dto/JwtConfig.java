package org.start2do.dto;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.start2do.util.JwtTokenUtil;

@Slf4j
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;

    @PostConstruct
    public void init() {
        log.info(this.secret);
        JwtTokenUtil.SECRET = secret;
    }
}
