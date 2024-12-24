package org.start2do;

import javax.sql.DataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConditionalOnProperty(prefix = "start2do.ebean", name = "multiple-data-sources", havingValue = "true")
@ConfigurationProperties(prefix = "start2do.datasource")
@ConditionalOnMissingBean(DataSourceProperties.class)
public class EbeanMultipleDataSourceConfiguration {

    private boolean force;
    private Class<? extends DataSource> type;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
}
