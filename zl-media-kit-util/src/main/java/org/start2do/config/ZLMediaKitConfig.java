package org.start2do.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "zl-media-kit")
public class ZLMediaKitConfig {

    private Boolean enable;
    private String host;
    private String ip;
    private String secret;
    private String vHost;
    private boolean isSSL = false;

    private WebHookSetting webHook;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class WebHookSetting {

        private Boolean enable;

    }

}
