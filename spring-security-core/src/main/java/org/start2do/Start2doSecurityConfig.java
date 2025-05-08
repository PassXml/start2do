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
    private long jwtTokenValidity = Long.valueOf(5 * 60 * 60);
    private Boolean checkExpired;

    private String secret;
    private Boolean mockUser = false;
    private String mockUserId = "1";
    private Integer tenantId = 1;
    private String mockUserName = "admin";
    @Value("${spring.main.web-application-type:SERVLET}")
    private WebApplicationType webApplicationType;

    /**
     * 记录登录尝试登录日志
     */
    private Boolean recordLoginLog = false;

    /**
     * 登录密码加密选型
     */
    private LoginPasswordEncryptConfig passwordEncryptConfig;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class LoginPasswordEncryptConfig {

        private boolean enabled;
        private Type type = Type.SM2;
        private String sm2publicKey;
        private String sm2privateKey;

    }

    public enum Type {
        SM2, ASE
    }

}
