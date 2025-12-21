package org.start2do.plugin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.start2do.plugin.spring.SafeParameterNameDiscoverer;

/**
 * 插件环境参数名解析容错配置。
 * <p>
 * 目标：避免 PF4J 插件 Controller/Bean 在启动或请求期，因为 Spring 解析参数名失败而阻断插件运行。
 */
@Slf4j
@Configuration
@ConditionalOnClass(ParameterNameDiscoverer.class)
public class PluginSafeParameterNameDiscovererAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor pluginSafeParameterNameDiscovererBeanFactoryPostProcessor() {
        return beanFactory -> {
            // 仅在插件上下文中容错；非插件上下文仍会保持抛错
            ParameterNameDiscoverer safe = new SafeParameterNameDiscoverer(new DefaultParameterNameDiscoverer());
            if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
                ((AbstractAutowireCapableBeanFactory) beanFactory).setParameterNameDiscoverer(safe);
                log.info("已安装 SafeParameterNameDiscoverer 到 BeanFactory: {}", beanFactory.getClass().getName());
            } else {
                log.warn("当前 BeanFactory 不支持 setParameterNameDiscoverer，跳过安装: {}", beanFactory.getClass().getName());
            }
        };
    }

    @Bean
    @ConditionalOnClass(RequestMappingHandlerAdapter.class)
    public static BeanPostProcessor pluginSafeParameterNameDiscovererWebMvcPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof RequestMappingHandlerAdapter) {
                    ((RequestMappingHandlerAdapter) bean).setParameterNameDiscoverer(
                        new SafeParameterNameDiscoverer(new DefaultParameterNameDiscoverer()));
                    log.info("已安装 SafeParameterNameDiscoverer 到 RequestMappingHandlerAdapter: beanName={}", beanName);
                }
                return bean;
            }
        };
    }
}
