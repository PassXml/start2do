package org.start2do;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.start2do.ebean.EbeanBeanPersistController;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;
import org.start2do.ebean.id_generators.UUIDStrIdGenerator;
import org.start2do.util.Snowflake;
import org.start2do.util.StringUtils;

@Slf4j
@Import({EbeanConfig.class, EbeanMultipleDataSourceConfiguration.class})
@ConditionalOnProperty(prefix = "start2do.ebean", name = "enable", havingValue = "true")
@RequiredArgsConstructor
@ComponentScans(value = {@ComponentScan(value = "org.start2do.ebean"),})
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
    @ConditionalOnMissingBean(EbeanBeanPersistController.class)
    public EbeanBeanPersistController beanPersistController() {
        return new EbeanBeanPersistController();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    @ConditionalOnBean(ObjectMapper.class)
    @ConditionalOnMissingBean(DatabaseConfig.class)
    public DatabaseConfig databaseConfig2(DataSource dataSource, CurrentUserProvider currentUserProvider,
        Snowflake snowflake, ObjectMapper objectMapper, EbeanBeanPersistController beanPersistController) {
        DatabaseConfig config = new DatabaseConfig();
        config.loadFromProperties();
        config.add(new SnowflakeStrGenerator(snowflake));
        config.add(new UUIDStrIdGenerator());
        config.setCurrentUserProvider(currentUserProvider);
        config.setRunMigration(ebeanConfig.isMigration());
        config.setDataSource(dataSource);
        config.setDdlRun(false);
        config.setObjectMapper(objectMapper);
        config.setExternalTransactionManager(new SpringJdbcTransactionManager());
        config.add(beanPersistController);
        config.setDdlCreateOnly(false);
        if (ebeanConfig.isMigration()) {
            migration(dataSource);
        }
        return config;
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    @ConditionalOnMissingBean(DatabaseConfig.class)
    public DatabaseConfig databaseConfig1(DataSource dataSource, CurrentUserProvider currentUserProvider,
        Snowflake snowflake, EbeanBeanPersistController beanPersistController) {
        DatabaseConfig config = new DatabaseConfig();
        config.loadFromProperties();
        config.add(new SnowflakeStrGenerator(snowflake));
        config.add(new UUIDStrIdGenerator());
        config.setCurrentUserProvider(currentUserProvider);
        config.setRunMigration(ebeanConfig.isMigration());
        config.setDataSource(dataSource);
        config.add(beanPersistController);
        config.setDdlRun(false);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        config.setObjectMapper(objectMapper);
        config.setExternalTransactionManager(new SpringJdbcTransactionManager());
        config.setDdlCreateOnly(false);
        if (ebeanConfig.isMigration()) {
            migration(dataSource);
        }
        return config;
    }

    @Bean
    @ConditionalOnMissingBean(Snowflake.class)
    public Snowflake snowflake() {
        return new Snowflake(1);
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

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "start2do.datasource", name = "force", havingValue = "true")
    @ConditionalOnMissingBean(DataSourceProperties.class)
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
    @ConditionalOnBean(DataSourceProperties.class)
    @ConditionalOnProperty(prefix = "start2do.ebean", name = "multiple-data-sources", havingValue = "true")
    public DataSource dataSource3(DataSourceProperties property) {
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
    public DataSource dataSource2(HikariConfig config) {
        log.info("使用HikariConfig初始化DataSource:{}", config.getJdbcUrl());
        return new HikariDataSource(config);
    }
}
