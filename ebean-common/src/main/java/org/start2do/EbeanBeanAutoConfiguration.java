package org.start2do;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.start2do.ebean.id_generators.UUIDStrIdGenerator;

@Import(EbeanConfig.class)
@AutoConfiguration
@ConditionalOnProperty(name = "spring.datasource.url")
@RequiredArgsConstructor
@ComponentScans(value = {
    @ComponentScan(value = "org.start2do.ebean"),
})
public class EbeanBeanAutoConfiguration {


    public static void migration(DataSource dataSource, EbeanConfig ebeanConfig) {
        if (!ebeanConfig.isMigration()) {
            return;
        }
        MigrationConfig config = new MigrationConfig();
        MigrationRunner runner = new MigrationRunner(config);
        runner.run(dataSource);
    }

    @ConditionalOnBean(value = {ObjectMapper.class})
    @ConditionalOnMissingBean(DatabaseConfig.class)
    public static class Config1 {


        @Bean
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


    @ConditionalOnMissingBean({DatabaseConfig.class, ObjectMapper.class})
    public static class Config2 {

        @Bean
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


}
