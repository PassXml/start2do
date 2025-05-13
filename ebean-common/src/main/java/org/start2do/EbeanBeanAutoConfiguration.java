package org.start2do;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.start2do.ebean.config.EBeanProcessConfiguration;
import org.start2do.ebean.config.EbeanBeanPersistController;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;
import org.start2do.ebean.id_generators.UUIDStrIdGenerator;
import org.start2do.util.Snowflake;

@Slf4j
@Import(EbeanConfig.class)
@AutoConfiguration
@ConditionalOnProperty(name = "spring.datasource.url")
@RequiredArgsConstructor
@ComponentScans(value = {@ComponentScan(value = "org.start2do.ebean"),})
public class EbeanBeanAutoConfiguration {


    public static void migration(DataSource dataSource, EbeanConfig ebeanConfig) {
        if (!ebeanConfig.isMigration()) {
            return;
        }
        MigrationConfig config = new MigrationConfig();
        MigrationRunner runner = new MigrationRunner(config);
        runner.run(dataSource);
    }


    public static class Config1 {


        @Bean
        @ConditionalOnBean(value = {ObjectMapper.class})
        @ConditionalOnMissingBean(DatabaseConfig.class)
        @ConditionalOnProperty(prefix = "start2do.ebean", name = "enable-hooks", havingValue = "false")
        public DatabaseConfig databaseConfig(DataSource dataSource, CurrentUserProvider currentUserProvider,
            ObjectMapper objectMapper, EbeanConfig ebeanConfig) {
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
                EbeanBeanAutoConfiguration.migration(dataSource, ebeanConfig);
            }
            config.setObjectMapper(objectMapper);
            return config;
        }
    }


    public static class Config2 {

        @Bean
        @ConditionalOnMissingBean({DatabaseConfig.class, ObjectMapper.class})
        @ConditionalOnProperty(prefix = "start2do.ebean", name = "enable-hooks", havingValue = "false")
        public DatabaseConfig databaseConfig(DataSource dataSource, CurrentUserProvider currentUserProvider,
            EbeanConfig ebeanConfig) {
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
                EbeanBeanAutoConfiguration.migration(dataSource, ebeanConfig);
            }
            return config;
        }

    }


    public static class Config5 {

        @Bean
        @ConditionalOnProperty(prefix = "start2do.ebean", name = "enable-hooks", havingValue = "true")
        @ConditionalOnMissingBean({DatabaseConfig.class, ObjectMapper.class, EBeanProcessConfiguration.class})
        public DatabaseConfig databaseConfig(DataSource dataSource, CurrentUserProvider currentUserProvider,
            EbeanConfig ebeanConfig, List<EBeanProcessConfiguration> configuration) {
            DatabaseConfig config = new DatabaseConfig();
            config.loadFromProperties();
            config.add(new UUIDStrIdGenerator());
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(ebeanConfig.isMigration());
            config.setDataSource(dataSource);
            config.setDdlRun(false);
            config.setExternalTransactionManager(new SpringJdbcTransactionManager());
            config.setDdlCreateOnly(false);
            for (EBeanProcessConfiguration processConfiguration : configuration) {
                processConfiguration.after(config);
            }
            if (ebeanConfig.isMigration()) {
                EbeanBeanAutoConfiguration.migration(dataSource, ebeanConfig);
            }
            return config;
        }

    }

    @Bean
    @ConditionalOnMissingBean(CurrentUserProvider.class)
    public CurrentUserProvider currentUserProvider() {
        return () -> "not set";
    }

    @Bean
    @ConditionalOnMissingBean(Database.class)
    @ConditionalOnBean(value = {DatabaseConfig.class})
    public io.ebean.Database database(DatabaseConfig config) {
        return DatabaseFactory.create(config);
    }


    public static class Config3 {


        @Bean
        @ConditionalOnProperty(prefix = "start2do.ebean", name = "enable-hooks", havingValue = "false")
        @ConditionalOnBean(value = {ObjectMapper.class, Snowflake.class})
        @ConditionalOnMissingBean(DatabaseConfig.class)
        public DatabaseConfig databaseConfig(DataSource dataSource, CurrentUserProvider currentUserProvider,
            ObjectMapper objectMapper, EbeanConfig ebeanConfig, Snowflake snowflake) {
            DatabaseConfig config = new DatabaseConfig();
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
    }

    public static class Config4 {


        @Bean
        @ConditionalOnProperty(prefix = "start2do.ebean", name = "enable-hooks", havingValue = "true")
        @ConditionalOnBean(value = {ObjectMapper.class, Snowflake.class})
        @ConditionalOnMissingBean(DatabaseConfig.class)
        public DatabaseConfig databaseConfig(DataSource dataSource, CurrentUserProvider currentUserProvider,
            ObjectMapper objectMapper, EbeanConfig ebeanConfig, Snowflake snowflake,
            List<EBeanProcessConfiguration> eBeanProcessConfiguration) {
            DatabaseConfig config = new DatabaseConfig();
            config.loadFromProperties();
            config.add(new UUIDStrIdGenerator());
            config.add(new SnowflakeStrGenerator(snowflake));
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(ebeanConfig.isMigration());
            config.setDataSource(dataSource);
            config.setDdlRun(false);
            config.setExternalTransactionManager(new SpringJdbcTransactionManager());
            config.setDdlCreateOnly(false);
            for (EBeanProcessConfiguration configuration : eBeanProcessConfiguration) {
                configuration.after(config);
            }
            if (ebeanConfig.isMigration()) {
                EbeanBeanAutoConfiguration.migration(dataSource, ebeanConfig);
            }
            config.setObjectMapper(objectMapper);
            return config;
        }
    }



    @Bean
    public EBeanProcessConfiguration eBeanProcessConfiguration(EbeanConfig ebeanConfig) {
        return t -> {
            if (ebeanConfig.isEnableHooks()) {
                log.info("EBeanProcessConfiguration开启Hooks");
                t.add(new EbeanBeanPersistController());
            }
        };
    }
}
