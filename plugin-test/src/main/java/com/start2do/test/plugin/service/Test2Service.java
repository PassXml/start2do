package com.start2do.test.plugin.service;

import org.springframework.stereotype.Service;
import org.start2do.plugin.api.spring.annotation.PluginBean;

@PluginBean
@Service
public class Test2Service {


    public String version() {
        return "2025年12月10日16:02:26";
    }

}
