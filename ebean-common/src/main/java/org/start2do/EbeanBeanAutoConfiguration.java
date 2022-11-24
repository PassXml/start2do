package org.start2do;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.start2do.ebean.id_generators.UUIDStrIdGenerator;

@Configuration
public class EbeanBeanAutoConfiguration {

    @Value("${migration:false}")
    private static boolean migration = false;

    @Configuration
    @ConditionalOnProperty(name = "spring.datasource.username")
    @AutoConfigureAfter(Order2.class)
    public static class Order3 {

        @Bean
        @ConditionalOnMissingBean(Database.class)
        public Database database(DataSource dataSource, CurrentUserProvider currentUserProvider) {
            DatabaseConfig config = new DatabaseConfig();
            config.loadFromProperties();
            config.add(new UUIDStrIdGenerator());
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
    @AutoConfigureBefore(Order3.class)
    @AutoConfigureAfter(Order1.class)
    public static class Order2 {

        @Bean
        @ConditionalOnMissingBean(Database.class)
        @ConditionalOnBean(value = {DataSource.class, CurrentUserProvider.class})
        public Database database(DataSource dataSource, CurrentUserProvider currentUserProvider) {
            DatabaseConfig config = new DatabaseConfig();
            config.loadFromProperties();
            config.add(new UUIDStrIdGenerator());
            config.setCurrentUserProvider(currentUserProvider);
            config.setRunMigration(migration);
            config.setDataSource(dataSource);
            Database database = DatabaseFactory.create(config);
            return database;
        }

    }

    @Configuration
    @AutoConfigureBefore({Order2.class, Order3.class})
    @ConditionalOnProperty(name = "spring.datasource.username")
    public static class Order1 {

        @Bean
        @ConditionalOnBean(value = DatabaseConfig.class)
        @ConditionalOnMissingBean(value = io.ebean.Database.class)
        public Database database(DatabaseConfig config) {
            Database database = DatabaseFactory.create(config);
            return database;
        }
    }


}
