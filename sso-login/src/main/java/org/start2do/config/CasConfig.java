package org.start2do.config;

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
@ConditionalOnProperty(value = "cas.enable", havingValue = "true")
@ConfigurationProperties(prefix = "cas")
public class CasConfig {

    private Boolean enable;
    private String casUrl;
    private Boolean isHttpSecurity = false;
    /**
     * cas 服务端ip 地址;可能存在和casUrl不一致的情况
     */
    private String casServerUrl = casUrl;
    private String casLoginUri = "/login";
    private String casLogoutUri = "/logout";
    private String baseUrl;
    private String loginUri = "/login/cas";
    private String successUrl = "/";

    private String logoutUri = "/logout/cas";
    private String key;

    //    失败的地址
    public static final String unauthorizedUrl = "/error/exthrow";
    private String[] whileList;

    public String getCasServerUrl() {
        if (casServerUrl == null || casServerUrl.isEmpty()) {
            casServerUrl = casUrl;
        }
        return casServerUrl;
    }
}
