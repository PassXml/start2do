package org.start2do;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.business.minio")
public class MinioConfig {

    private boolean enable;

    private List<Item> items;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Item {

        private Boolean isDefault = false;
        private String name;


        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(60);
        private Duration connectTimeout = Duration.ofSeconds(30);


        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
        /**
         * 基础路径
         */
        private String basePath;
        /**
         * 回显的域名
         */
        private String echoUrl;
        private boolean checkBucket = true;
        private boolean createBucket = true;

        /**
         * 允许匿名访问
         */
        private boolean anomyousAccess = true;
    }
}
