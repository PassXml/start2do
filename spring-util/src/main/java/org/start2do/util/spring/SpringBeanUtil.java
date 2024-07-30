package org.start2do.util.spring;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPattern;


@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public final class SpringBeanUtil implements BeanFactoryAware, ApplicationContextAware {

    @Getter
    private static ApplicationContext context;
    @Getter
    private static ConfigurableBeanFactory beanFactory;

    public static void registerBean(String beanName, Object object) {
        beanFactory.registerSingleton(beanName, object);
    }

    public static <T> T getBean(Class<T> tClass) {
        return context.getBean(tClass);
    }

    public static <T> T getBeanFormBeanFactory(Class<T> tClass) {
        return beanFactory.getBean(tClass);
    }

    public static <T> Map<String, T> getBeans(Class<T> tClass) {
        return context.getBeansOfType(tClass);
    }


    public static <T> T getBean(String className, Class<T> tClass) {
        return context.getBean(className, tClass);
    }

    public static <T> T getBeanFromBeanFactory(String className, Class<T> tClass) {
        return beanFactory.getBean(className, tClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringBeanUtil.context = context;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        SpringBeanUtil.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }

    public static Set<String> getWebUrlsByReactive() {
        return getWebUrlsByReactive(null);
    }

    public static Set<String> getWebUrlsByReactive(Function<HandlerMethod, Boolean> filter) {
        Map<String, org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping> map = SpringBeanUtil.getBeans(
            org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping.class);
        Set<String> result = new HashSet<>();
        for (org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping value : map.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = value.getHandlerMethods();
            for (RequestMappingInfo info : handlerMethods.keySet()) {
                if (filter != null) {
                    if (!filter.apply(handlerMethods.get(info))) {
                        continue;
                    }
                }
                for (PathPattern pattern : info.getPatternsCondition().getPatterns()) {
                    result.add(pattern.getPatternString());
                }
            }
        }
        return result;
    }
}
