package org.start2do.bpm.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.start2do.bpm.interfaces.ITokenUtil;

@Slf4j
@ComponentScan("org.start2do.bpm")
@RequiredArgsConstructor
public class BpmAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ITokenUtil.class)
    public ITokenUtil tokenUtil() {
        return new ITokenUtil() {
        };
    }
}
