package org.start2do;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.click-house")
public class ClickHouseConfig {

    private Boolean enable;
    private String name;
    private String url;
    private String username;
    private String password;
    private String driveClass = "com.clickhouse.jdbc.ClickHouseDriver";
}
