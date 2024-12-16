package org.start2do;

import javax.sql.DataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "start2do.click-house")
public class ClickHouseConfig {

    private Boolean enable;
    private String url;
    private String username;
    private String password;
    private Class<? extends DataSource> type;
    private String driverClassName = "com.clickhouse.jdbc.ClickHouseDriver";
    private String configLocation = "classpath:mybatis/mybatis-config.xml";
    private String mapperLocations = "classpath*:mybatis/clickhouse/*.xml";
    private String basePackage = "com.zte.clickhouse";

}
