package org.start2do;


import java.util.concurrent.Executors;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.start2do.service.SysLogService;
import org.start2do.util.spring.LogAop;
import org.start2do.util.spring.LogAopConfig;

@Import(BusinessConfig.class)
@EnableAutoConfiguration
@ConditionalOnProperty(prefix = "start2do.business", value = "enable", havingValue = "true")
public class BusinessAutoConfig {

    @Bean
    @ConditionalOnProperty(prefix = "start2do.business.sys-log", value = "enable", havingValue = "true")
    public SysLogAop sysLogAop(SysLogService sysLogService, LogAop.JSON json, LogAopConfig config) {
        return new SysLogAop(sysLogService, config, json, Executors.newFixedThreadPool(5));
    }
}
