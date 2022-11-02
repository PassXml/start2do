package org.start2do;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EbeanBeanAutoConfiguration {

    @Value("${migration:false}")
    private static boolean migration = false;

    @Configuration
    @ConditionalOnProperty(name = "spring.datasource.username")
    @AutoConfigureAfter(Order1_5.class)
    public static class Order1 {

        @Bean
        @ConditionalOnMissingBean(Database.class)
        public Database database(DataSource dataSource, CurrentUserProvider currentUserProvider) {
            DatabaseConfig config = new DatabaseConfig();
            config.loadFromProperties();
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(migration);
            config.setDataSource(dataSource);
            Database database = DatabaseFactory.create(config);
            return database;
        }

        @Bean
        @ConditionalOnMissingBean(CurrentUserProvider.class)
        public CurrentUserProvider currentUserProvider() {
            return () -> "not set";
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "spring.datasource.username")
    @AutoConfigureAfter(Order2.class)
    public static class Order1_5 {

        @Bean
        @ConditionalOnMissingBean(Database.class)
        public Database database(DataSource dataSource, CurrentUserProvider currentUserProvider) {
            DatabaseConfig config = new DatabaseConfig();
            config.loadFromProperties();
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(migration);
            config.setDataSource(dataSource);
            Database database = DatabaseFactory.create(config);
            return database;
        }

    }

    @Configuration
    @ConditionalOnProperty(name = "spring.datasource.username")
    public static class Order2 {

        @Bean
        @ConditionalOnMissingBean(value = io.ebean.Database.class)
        public Database database(DatabaseConfig config) {
            Database database = DatabaseFactory.create(config);
            return database;
        }
    }


}
