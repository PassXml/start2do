package org.start2do;


import com.clickhouse.client.config.ClickHouseDefaults;
import com.clickhouse.jdbc.ClickHouseDataSource;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.clickhouse.ClickHousePlatform;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Import(ClickHouseConfig.class)
@ConditionalOnProperty(prefix = "start2do.click-house", name = "enable", havingValue = "true")
@RequiredArgsConstructor
public class EbeanClickHouseAutoConfig {

    public static final String ClickHouseEbeanDatabase = "ClickHouseEbeanDatabase";

    @Bean(ClickHouseEbeanDatabase)
    public Database ClickHouseDatabase(CurrentUserProvider currentUserProvider,
        @Qualifier("ClickHouseDatabase") DataSource dataSource) {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setName(ClickHouseEbeanDatabase);
        databaseConfig.setDataSource(dataSource);
        databaseConfig.setDefaultServer(false);
        databaseConfig.setExternalTransactionManager(new SpringJdbcTransactionManager());
        databaseConfig.setDatabasePlatform(new ClickHousePlatform());
        databaseConfig.setCurrentUserProvider(currentUserProvider);
        return DatabaseFactory.create(databaseConfig);
    }

    @Bean({"ClickHouseDatabase", "CLickHouseDataSource"})
    public DataSource clickHouseDatabase(ClickHouseConfig config) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(ClickHouseDefaults.USER.getKey(), config.getUsername());
        properties.setProperty(ClickHouseDefaults.PASSWORD.getKey(), config.getPassword());
        return new ClickHouseDataSource(config.getUrl(), properties);
    }

    @Bean("ClickHouseSessionFactory")
    public SqlSessionFactory clickHouseSessionFactory(@Qualifier("ClickHouseDatabase") DataSource dataSource,
        ClickHouseConfig config) throws Exception {
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        Resource[] mapperRes = new PathMatchingResourcePatternResolver().getResources(config.getMapperLocations());
        Resource configRes = new PathMatchingResourcePatternResolver().getResource(config.getConfigLocation());
        return MybatisDatasourceFactory.sqlSessionFactory(null, dataSource, mapperRes, configRes);
    }


    @Bean("ClickhouseSessionTemplate")
    public SqlSessionTemplate clickhouseSessionTemplate(
        @Qualifier("ClickHouseSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return MybatisDatasourceFactory.sqlSessionTemplate(sqlSessionFactory);
    }


}
