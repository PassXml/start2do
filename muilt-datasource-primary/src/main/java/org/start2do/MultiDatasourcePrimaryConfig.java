package org.start2do;


import javax.sql.DataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.datasource")
public class MultiDatasourcePrimaryConfig {

    private Class<DataSource> type;
    private String driverClassName;
    private String username;
    private String password;
    private String url;
}
