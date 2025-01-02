package org.start2do;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ComponentScan("org.start2do")
@Import(SpringCommonConfig.class)
public class AutoScanConfig {

}
