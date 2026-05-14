package com.start2do.test.plugin.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.start2do.plugin.api.spring.annotation.PluginBean;

@PluginBean
@Service
public class Test2Service {

    @Value("${pluginTest.greeting:来自插件默认值}")
    private String greeting;

    @PostConstruct
    public void init() {
        System.out.println("测试生命周期");
    }
    @PreDestroy
    public void destroy() {
        System.out.println("123销毁");
    }

    public String version() {
        return greeting + " | 2025年12月10日16:02:26";
    }

    public String greeting() {
        return greeting;
    }
}
