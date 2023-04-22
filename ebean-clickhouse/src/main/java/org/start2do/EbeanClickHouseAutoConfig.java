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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Import(EbeanClickHouseConfig.class)
@ConditionalOnProperty(prefix = "start2do.click-house", name = "enable", havingValue = "true")
@RequiredArgsConstructor
public class EbeanClickHouseAutoConfig {

    public static final String ClickHouseSessionFactory = "ClickHouseSessionFactory";
    public static final String ClickHouseEbeanDatabase = "ClickHouseEbeanDatabase";

    @Bean(name = "ClickHouseEbeanDatabase")
    public Database ClickHouseDatabase(CurrentUserProvider currentUserProvider,
        @Qualifier("ClickHouseDatabase") DataSource dataSource) {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setName(ClickHouseEbeanDatabase);
        databaseConfig.setDataSource(dataSource);
        databaseConfig.setExternalTransactionManager(new SpringJdbcTransactionManager());
        databaseConfig.setDatabasePlatform(new ClickHousePlatform());
        databaseConfig.setCurrentUserProvider(currentUserProvider);
        return DatabaseFactory.create(databaseConfig);
    }

    @Bean("ClickHouseDatabase")
    public DataSource cLickHouseDataSource(EbeanClickHouseConfig config) throws SQLException {
//        return DataSourceBuilder.create().url(config.getUrl()).username(config.getUsername())
//            .password(config.getPassword()).driverClassName(config.getDriveClass()).build();
        Properties properties = new Properties();
        properties.setProperty(ClickHouseDefaults.USER.getKey(), config.getUsername());
        properties.setProperty(ClickHouseDefaults.PASSWORD.getKey(), config.getPassword());
        // properties.setProperty("ssl", "true");
// properties.setProperty("sslmode", "NONE"); // NONE to trust all servers; STRICT for trusted only
        ClickHouseDataSource dataSource = new ClickHouseDataSource(config.getUrl(), properties);
        return dataSource;
    }

    @Bean("ClickHouseSessionFactory")
    public SqlSessionFactory clickHouseSessionFactory(@Qualifier("ClickHouseDatabase") DataSource dataSource,
        EbeanClickHouseConfig config) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        List<Resource> locations = new ArrayList<>();
        for (Resource resource : new PathMatchingResourcePatternResolver().getResources(
            "classpath*:mybatis/clickhouse/*.xml")) {
            locations.add(resource);
        }
        bean.setMapperLocations(locations.toArray(new Resource[]{}));
        bean.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));
        return bean.getObject();
    }


    @Bean("ClickhouseSessionTemplate")
    public SqlSessionTemplate clickhouseSessionTemplate(
        @Qualifier("ClickHouseSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
