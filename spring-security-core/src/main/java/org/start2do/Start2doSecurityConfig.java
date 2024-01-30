package org.start2do;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class Start2doSecurityConfig {

    private Boolean enable;
    private List<String> whiteList;

    private Boolean checkExpired;

    private String secret;
    private Boolean mockUser = false;
    private Integer mockUserId = 1;
    private Integer tenantId = 1;
    private String mockUserName = "admin";
    @Value("${spring.main.web-application-type}")
    private WebApplicationType webApplicationType;

}
