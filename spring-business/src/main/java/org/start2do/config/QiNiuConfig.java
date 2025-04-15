package org.start2do.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@AutoConfiguration
@ConfigurationProperties(prefix = "start2do.business.file-setting.qiniu")
public class QiNiuConfig {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String baseDir;
}
