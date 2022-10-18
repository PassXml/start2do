package org.start2do.util.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public final class SpringBeanUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    public static <T> T getBean(Class<T> tClass) {
        return context.getBean(tClass);
    }

    public static <T> T getBean(String className, Class<T> tClass) {
        return context.getBean(className, tClass);
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringBeanUtil.context = context;

    }
}
