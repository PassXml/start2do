package org.start2do.util.spring;

import java.util.Map;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public final class SpringBeanUtil implements ApplicationContextAware {

    @Getter
    private static ApplicationContext context;


    public static <T> T getBean(Class<T> tClass) {
        return context.getBean(tClass);
    }

    public static <T> Map<String, T> getBeans(Class<T> tClass) {
        return context.getBeansOfType(tClass);
    }

    public static <T> T getBean(String className, Class<T> tClass) {
        return context.getBean(className, tClass);
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringBeanUtil.context = context;
    }
}
