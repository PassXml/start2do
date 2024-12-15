package org.start2do;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;
import org.start2do.ebean.id_generators.UUIDStrIdGenerator;
import org.start2do.ebean.util.Snowflake;
import org.start2do.util.StringUtils;

@Slf4j
@Import({EbeanConfig.class, EbeanMultipleDataSourceConfiguration.class})
@ConditionalOnProperty(name = "spring.datasource.url")
@RequiredArgsConstructor
@ComponentScans(value = {
    @ComponentScan(value = "org.start2do.ebean"),
})
public class EbeanBeanAutoConfiguration {

    private final EbeanConfig ebeanConfig;

    private void migration(DataSource dataSource) {
        if (!ebeanConfig.isMigration()) {
            return;
        }
        MigrationConfig config = new MigrationConfig();
        MigrationRunner runner = new MigrationRunner(config);
        runner.run(dataSource);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    @ConditionalOnMissingBean(DatabaseConfig.class)
    public DatabaseConfig databaseConfig(@Qualifier("dataSource") DataSource dataSource,
        CurrentUserProvider currentUserProvider) {
        DatabaseConfig config = new DatabaseConfig();
        config.loadFromProperties();
        config.add(new UUIDStrIdGenerator());
        config.setCurrentUserProvider(currentUserProvider);
        config.setRunMigration(ebeanConfig.isMigration());
        config.setDataSource(dataSource);
        config.setDdlRun(false);
        config.setExternalTransactionManager(new SpringJdbcTransactionManager());
        config.setDdlCreateOnly(false);
        if (ebeanConfig.isMigration()) {
            migration(dataSource);
        }
        return config;
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    @ConditionalOnMissingBean(DatabaseConfig.class)
    public DatabaseConfig databaseConfig(@Qualifier("dataSource") DataSource dataSource,
        CurrentUserProvider currentUserProvider,
        Snowflake snowflake) {
        DatabaseConfig config = new DatabaseConfig();
        config.loadFromProperties();
        config.add(new SnowflakeStrGenerator(snowflake));
        config.add(new UUIDStrIdGenerator());
        config.setCurrentUserProvider(currentUserProvider);
        config.setRunMigration(ebeanConfig.isMigration());
        config.setDataSource(dataSource);
        config.setDdlRun(false);
        config.setExternalTransactionManager(new SpringJdbcTransactionManager());
        config.setDdlCreateOnly(false);
        if (ebeanConfig.isMigration()) {
            migration(dataSource);
        }
        return config;
    }

    @Bean
    @ConditionalOnMissingBean(CurrentUserProvider.class)
    public CurrentUserProvider currentUserProvider() {
        return () -> "not set";
    }

    @Bean
    @Primary
    @ConditionalOnBean(value = {DatabaseConfig.class})
    public io.ebean.Database database(DatabaseConfig config) {
        return DatabaseFactory.create(config);
    }

    @Bean("dataSource")
    @Primary
    @ConditionalOnProperty(prefix = "start2do.ebean", name = "multiple-data-sources", havingValue = "true")
    public DataSource dataSource(EbeanMultipleDataSourceConfiguration property) {
        log.info("初始化DataSource:{}", property.getUrl());
        HikariConfig config = new HikariConfig();
        config.setUsername(property.getUsername());
        config.setPassword(property.getPassword());
        config.setJdbcUrl(property.getUrl());
        if (StringUtils.isNotEmpty(property.getDriverClassName())) {
            config.setDriverClassName(property.getDriverClassName());
        }
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "start2do.ebean", name = "multiple-data-sources", havingValue = "true")
    @ConditionalOnBean(HikariConfig.class)
    public DataSource dataSource(HikariConfig config) {
        log.info("使用HikariConfig初始化DataSource:{}", config.getJdbcUrl());
        return new HikariDataSource(config);
    }

}
