package org.start2do.cep.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "start2do.cep")
public class FlinkConfig {

    private String jobName = "CEP_Job";
    private int parallelism = 1;
    private boolean enableCheckpoint = false;
    private long checkpointInterval = 60000; // 60 seconds

    private final HttpConfig http = new HttpConfig();
    private final KafkaConfig kafka = new KafkaConfig();

    @Data
    public static class HttpConfig {
        private boolean enabled = true;
        private int port = 8081;
        private String contextPath = "/";
    }

    @Data
    public static class KafkaConfig {
        private boolean enabled = false;
        private String bootstrapServers = "localhost:9092";
        private String topic = "cep-events";
        private String groupId = "cep-flink-group";
    }
}
