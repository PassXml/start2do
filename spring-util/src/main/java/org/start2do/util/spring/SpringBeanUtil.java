package org.start2do.util.spring;

import java.util.Map;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


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

    public static <T> ObjectProvider<T> getBeanProvider(Class<T> tClass) {
        return context.getBeanProvider(tClass);
    }

    public static boolean containsBean(String tClass) {
        return context.containsBean(tClass);
    }

    public static String[] getBeanNamesForType(Class<?> tClass) {
        return context.getBeanNamesForType(tClass);
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

    public <T> T registerBean(String name, Class<T> clazz,
        Object... args) {
        if (context.containsBean(name)) {
            Object bean = context.getBean(name);
            if (bean.getClass().isAssignableFrom(clazz)) {
                return (T) bean;
            } else {
                throw new RuntimeException("BeanName 重复 " + name);
            }
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) context.getParentBeanFactory();
        beanFactory.registerBeanDefinition(name, beanDefinition);
        return context.getBean(name, clazz);
    }
}
