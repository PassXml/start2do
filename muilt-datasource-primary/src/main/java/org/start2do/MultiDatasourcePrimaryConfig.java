package org.start2do;


import javax.sql.DataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MultiDatasourcePrimaryConfig {

    private Class<DataSource> type;
    private String driverClassName;
    private String username;
    private String password;
    private String url;
}
