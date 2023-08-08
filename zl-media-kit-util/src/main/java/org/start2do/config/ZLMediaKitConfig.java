package org.start2do.config;

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
@ConfigurationProperties(prefix = "zl-media-kit")
public class ZLMediaKitConfig {

    private Boolean enable;
    private String serverHost;
    private String pullHost;
    private String secret;
    private String vHost;
    private boolean ssl = false;

    private WebHookSetting webHook;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class WebHookSetting {

        private Boolean enable;

    }

}
