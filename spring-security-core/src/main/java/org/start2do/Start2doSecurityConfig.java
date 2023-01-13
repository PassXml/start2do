package org.start2do;

import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.start2do.util.JwtTokenUtil;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class Start2doSecurityConfig {

    private Boolean enable = true;
    private List<String> whiteList;

    private Boolean checkExpired;

    private String secret;

    @PostConstruct
    public void init() {
        JwtTokenUtil.SECRET = secret;
    }
}
