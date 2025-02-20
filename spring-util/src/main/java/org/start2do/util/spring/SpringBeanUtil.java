package org.start2do.util.spring;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.start2do.util.spring.dto.UrlInfo;
import org.start2do.util.spring.dto.UrlInfoDto;


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

    public <T> T registerBean(String name, Class<T> clazz, Object... args) {
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


    public static List<UrlInfoDto> getAllUrls() {
        Map<String, RequestMappingHandlerMapping> beans = getBeans(RequestMappingHandlerMapping.class);
        List<UrlInfoDto> result = new LinkedList<>();
        for (Entry<String, RequestMappingHandlerMapping> entry : beans.entrySet()) {
            RequestMappingHandlerMapping requestMappingHandlerMapping = entry.getValue();
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entryM : handlerMethods.entrySet()) {
                RequestMappingInfo mappingInfo = entryM.getKey();
                HandlerMethod handlerMethod = entryM.getValue();
                UrlInfo info = handlerMethod.getMethodAnnotation(UrlInfo.class);
                UrlInfoDto dto = new UrlInfoDto(
                    handlerMethod.getBeanType().getName(), handlerMethod.getMethod().getName(),
                    mappingInfo.getPatternValues()
                );
                if (info != null) {
                    dto.setUrlName(info.value());
                }
                result.add(dto);
            }
        }
        return result;
    }

    public static Set<String> getWebUrlsByReactive() {
        return getWebUrlsByReactive(null);
    }

    public static Set<String> getWebUrlsByReactive(Function<HandlerMethod, Boolean> filter) {
        Map<String, org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping> map = SpringBeanUtil.getBeans(
            org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping.class);
        Set<String> result = new HashSet<>();
        for (org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping value : map.values()) {
            Map<org.springframework.web.reactive.result.method.RequestMappingInfo, HandlerMethod> handlerMethods = value.getHandlerMethods();
            for (org.springframework.web.reactive.result.method.RequestMappingInfo info : handlerMethods.keySet()) {
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
