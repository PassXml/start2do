package com.start2do.test.plugin;

import com.start2do.test.plugin.controller.TestController;
import com.start2do.test.plugin.mapper.TestMapper;
import com.start2do.test.plugin.service.Test2Service;
import com.start2do.test.plugin.ws.HelloWebServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Collection;
import java.util.List;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.start2do.plugin.api.spring.MapperMeta;
import org.start2do.plugin.api.spring.MybatisDataSourceExtension;
import org.start2do.plugin.api.spring.MybatisMapperExtension;
import org.start2do.plugin.api.spring.PluginDatabaseMeta;
import org.start2do.plugin.api.spring.SoapWebServiceExtension;
import org.start2do.plugin.api.spring.SpringMvcControllerExtension;
import org.start2do.plugin.api.spring.SpringPluginBeansExtension;

@Extension
public class PluginConfig extends Plugin implements SpringMvcControllerExtension, SpringPluginBeansExtension,
    SoapWebServiceExtension, MybatisMapperExtension, MybatisDataSourceExtension {

    @Override
    public Collection<Class<?>> getControllerClasses() {
        return List.of(TestController.class);
    }

    @Override
    public Collection<Class<?>> getBeanClasses() {
        // 注册插件内部的 Service 以及 SOAP WebService 实现类为 Spring Bean
        // 顺序：先注册 Test2Service，再注册依赖它的 HelloWebServiceImpl
        return List.of(Test2Service.class, HelloWebServiceImpl.class);
    }

    @Override
    public Collection<WebServiceMeta> getWebServices() {
        return List.of(new WebServiceMeta(
            HelloWebServiceImpl.class, null, null, null, null
        ));
    }

    @Override
    public Collection<MapperMeta> getMappers() {
        return List.of(new MapperMeta("test", TestMapper.class));
    }

    @Override
    public Collection<PluginDatabaseMeta> getDatabases() {
        return List.of(new PluginDatabaseMeta(
            "test", "jdbc:mysql://192.168.30.130:3306/?useSSL=false", "root", "zxwz@123", "com.mysql.jdbc.Driver",
            HikariDataSource.class, null, "mysql"
        ));
    }
}
