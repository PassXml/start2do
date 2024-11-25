package org.start2do;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;
import org.start2do.ebean.id_generators.UUIDStrIdGenerator;
import org.start2do.ebean.service.SysSettingService;
import org.start2do.ebean.util.SysSettingUtil;
import org.start2do.util.Snowflake;

@Import(EbeanConfig.class)
@ConditionalOnProperty(name = "spring.datasource.url")
@RequiredArgsConstructor
public class EbeanBeanAutoConfiguration {


    public static void migration(DataSource dataSource, EbeanConfig ebeanConfig) {
        if (!ebeanConfig.isMigration()) {
            return;
        }
        MigrationConfig config = new MigrationConfig();
        MigrationRunner runner = new MigrationRunner(config);
        runner.run(dataSource);
    }

    @Order
    @Configuration
    @AutoConfigureAfter(DatasourceConfig.class)
    public static class DatabaseConfig {

        @Bean
        @Conditional(DefaultDatabaseConfig2.class)
        public io.ebean.config.DatabaseConfig databaseConfig(DataSource dataSource,
            CurrentUserProvider currentUserProvider,
            ObjectMapper objectMapper, EbeanConfig ebeanConfig) {
            io.ebean.config.DatabaseConfig config = new io.ebean.config.DatabaseConfig();
            config.loadFromProperties();
            config.add(new UUIDStrIdGenerator());
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(ebeanConfig.isMigration());
            config.setDataSource(dataSource);
            config.setDdlRun(false);
            config.setExternalTransactionManager(new SpringJdbcTransactionManager());
            config.setDdlCreateOnly(false);
            if (ebeanConfig.isMigration()) {
                EbeanBeanAutoConfiguration.migration(dataSource, ebeanConfig);
            }
            config.setObjectMapper(objectMapper);
            return config;
        }

        @Bean
        @Conditional(DefaultDatabase.class)
        public io.ebean.Database database(io.ebean.config.DatabaseConfig config) {
            return DatabaseFactory.create(config);
        }

        @Bean
        @Conditional(DatabaseConfigDefault.class)
        public io.ebean.config.DatabaseConfig databaseConfig(DataSource dataSource,
            CurrentUserProvider currentUserProvider,
            ObjectMapper objectMapper, EbeanConfig ebeanConfig, Snowflake snowflake) {
            io.ebean.config.DatabaseConfig config = new io.ebean.config.DatabaseConfig();
            config.loadFromProperties();
            config.add(new UUIDStrIdGenerator());
            config.add(new SnowflakeStrGenerator(snowflake));
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(ebeanConfig.isMigration());
            config.setDataSource(dataSource);
            config.setDdlRun(false);
            config.setExternalTransactionManager(new SpringJdbcTransactionManager());
            config.setDdlCreateOnly(false);
            if (ebeanConfig.isMigration()) {
                EbeanBeanAutoConfiguration.migration(dataSource, ebeanConfig);
            }
            config.setObjectMapper(objectMapper);
            return config;
        }

        @Bean
        @ConditionalOnBean(Database.class)
        public SysSettingUtil sysSettingUtil(SysSettingService sysSettingService) {
            return new SysSettingUtil(sysSettingService);
        }

        @Bean
        @ConditionalOnMissingBean({io.ebean.config.DatabaseConfig.class, ObjectMapper.class})
        public io.ebean.config.DatabaseConfig databaseConfig(DataSource dataSource,
            CurrentUserProvider currentUserProvider,
            EbeanConfig ebeanConfig) {
            io.ebean.config.DatabaseConfig config = new io.ebean.config.DatabaseConfig();
            config.loadFromProperties();
            config.add(new UUIDStrIdGenerator());
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(ebeanConfig.isMigration());
            config.setDataSource(dataSource);
            config.setDdlRun(false);
            config.setExternalTransactionManager(new SpringJdbcTransactionManager());
            config.setDdlCreateOnly(false);
            if (ebeanConfig.isMigration()) {
                EbeanBeanAutoConfiguration.migration(dataSource, ebeanConfig);
            }
            return config;
        }
    }

    public static class DefaultDatabaseConfig2 implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return hasBean(context, ObjectMapper.class) && !hasBean(context, io.ebean.config.DatabaseConfig.class);

        }
    }


    @Bean
    @ConditionalOnMissingBean(CurrentUserProvider.class)
    public CurrentUserProvider currentUserProvider() {
        return () -> "not set";
    }


    public static class DefaultDatabase implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return hasBean(context, io.ebean.config.DatabaseConfig.class) && !hasBean(context, Database.class);

        }
    }


    public static class DatabaseConfigDefault implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return hasBean(context, ObjectMapper.class) && hasBean(context, Snowflake.class) && !hasBean(context,
                io.ebean.config.DatabaseConfig.class);

        }
    }

    public static boolean hasBean(ConditionContext context, Class<?> clazz) {
        try {
            Object bean = context.getBeanFactory().getBean(clazz);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Configuration
    @AutoConfigureBefore(DatabaseConfig.class)
    public static class DatasourceConfig {

        @Bean
        @Conditional(DefaultDataSource.class)
        public DataSource dataSource(DataSourceProperties property) {
            HikariConfig config = new HikariConfig();
            config.setUsername(property.getUsername());
            config.setPassword(property.getPassword());
            config.setJdbcUrl(property.getUrl());
            config.setDriverClassName(property.getDriverClassName());
            return new HikariDataSource(config);
        }

        @Bean
        @Conditional(DefaultDataSource2.class)
        public DataSource dataSource(HikariConfig config) {
            return new HikariDataSource(config);
        }


    }


    public static class DefaultDataSource2 implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            boolean hasDataSource = hasBean(context, HikariDataSource.class);
            boolean hasConfig = hasBean(context, HikariConfig.class);
            return hasDataSource && hasConfig;
        }
    }


    public static class DefaultDataSource implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            boolean hasDataSource = hasBean(context, DataSource.class);
            boolean hasConfig = hasBean(context, HikariConfig.class);
            String property = context.getEnvironment().getProperty("spring.datasource.url");

            return !hasDataSource && !hasConfig && property != null;
        }
    }

}
