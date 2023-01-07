package org.start2do;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.start2do.ebean.id_generators.UUIDStrIdGenerator;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.username")
public class EbeanBeanAutoConfiguration {

    @Value("${migration:false}")
    private static boolean migration = false;


    @Bean
    @ConditionalOnMissingBean(DatabaseConfig.class)
    public DatabaseConfig databaseConfig(DataSource dataSource, CurrentUserProvider currentUserProvider) {
        DatabaseConfig config = new DatabaseConfig();
        config.loadFromProperties();
        config.add(new UUIDStrIdGenerator());
        config.setCurrentUserProvider(currentUserProvider);
        config.setRunMigration(migration);
        config.setDataSource(dataSource);
        config.setDdlRun(false);
        config.setDdlCreateOnly(false);
        return config;
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
        Database database = DatabaseFactory.create(config);
        return database;
    }


}
