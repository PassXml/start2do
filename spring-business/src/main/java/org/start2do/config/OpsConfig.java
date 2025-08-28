package org.start2do.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = "start2do.ops")
public class OpsConfig {

    private boolean enable = false;
    private String totpSecretKey;
    private List<String> whitePath = List.of("/tmp/safezone");
    /**
      *  备份目录
     */
    private String bakDir="/data/application/bak";

}
