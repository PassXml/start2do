package com.start2do.test.plugin.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.start2do.plugin.api.spring.annotation.PluginBean;

@PluginBean
@Service
public class Test2Service {

    @Value("${pluginTest.greeting:来自插件默认值}")
    private String greeting;

    public String version() {
        return greeting + " | 2025年12月10日16:02:26";
    }

    public String greeting() {
        return greeting;
    }
}
