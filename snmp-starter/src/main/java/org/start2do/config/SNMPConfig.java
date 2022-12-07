package org.start2do.config;

import java.util.List;
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
@ConfigurationProperties(prefix = "snmp")
@Configuration
@ConditionalOnProperty(prefix = "snmp", name = "enable", havingValue = "true")
public class SNMPConfig {

    /**
     * 监听地址
     */
    private String listen;
    private Integer version;
    private Long timeOut = 3000l;
    private List<String> oids;
    private V3Config v3;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class V3Config {

        private String user;
        private String password;
        private String privacyProtocol;
        private String authGeneric;
        private String privacyPassphrase;
    }


}
