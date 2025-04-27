package org.start2do.ebean.config;


import io.ebean.config.DatabaseConfig;

public interface EBeanProcessConfiguration {

    void after(DatabaseConfig ebeanConfig);
}
