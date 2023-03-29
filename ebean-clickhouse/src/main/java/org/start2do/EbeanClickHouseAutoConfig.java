package org.start2do;


import com.clickhouse.jdbc.ClickHouseDataSource;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.clickhouse.ClickHousePlatform;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import java.sql.SQLException;
import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(EbeanClickHouseConfig.class)
@ConditionalOnProperty(prefix = "start2do.click-house", name = "enable", havingValue = "true")
public class EbeanClickHouseAutoConfig {

    @Bean(name = "ClickHouseDatabase")
    public Database database(EbeanClickHouseConfig config) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("username", config.getUsername());
        properties.setProperty("password", config.getUsername());
        ClickHouseDataSource dataSource = new ClickHouseDataSource(config.getUrl(), properties);
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setDataSource(dataSource);
        databaseConfig.setExternalTransactionManager(new SpringJdbcTransactionManager());
        databaseConfig.setDatabasePlatform(new ClickHousePlatform());
        return DatabaseFactory.create(databaseConfig);
    }
}
