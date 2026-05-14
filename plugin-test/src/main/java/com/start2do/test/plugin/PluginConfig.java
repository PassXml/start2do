package com.start2do.test.plugin;

import com.zaxxer.hikari.HikariDataSource;
import org.pf4j.Plugin;
import org.start2do.plugin.api.spring.annotation.PluginDatabase;
import org.start2do.plugin.api.spring.annotation.PluginDatabases;
import org.start2do.plugin.api.spring.annotation.PluginDescriptor;

@PluginDescriptor(
    id = "org.start2do",
    version = "0.0.1",
    provider = "kiki",
    description = "你好"
)
@PluginDatabases(
    value = {
        @PluginDatabase(
            id = "ds1",
            url = "jdbc:mysql://192.168.30.130:3306/db1?useSSL=false",
            username = "root",
            password = "xxx",
            driverClassName = "com.mysql.jdbc.Driver",
            dataSourceType = HikariDataSource.class,
            mapperLocationPattern = "",
            dataType = "mysql"
        ),
        @PluginDatabase(
            id = "ds2",
            url = "jdbc:mysql://192.168.30.130:3306/db2?useSSL=false",
            username = "root",
            password = "yyy",
            driverClassName = "com.mysql.jdbc.Driver",
            dataSourceType = HikariDataSource.class,
            mapperLocationPattern = "",
            dataType = "mysql"
        )
    }
)
public class PluginConfig extends Plugin {

    @Override
    public void start() {

    }
}
