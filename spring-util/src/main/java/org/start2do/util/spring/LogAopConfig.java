package org.start2do.util.spring;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "start2do.log")
public class LogAopConfig {

    private boolean enable;

    private List<Class> skinClazz;
    private String name;

    @PostConstruct
    public void init() {
        if (skinClazz == null) {
            skinClazz = new ArrayList<>();
        }
        skinClazz.addAll(Arrays.asList(
            HttpServletRequest.class, HttpServletResponse.class,
            ServletResponse.class, ServletRequest.class, OutputStream.class,
            ByteArrayOutputStream.class
        ));
    }

}
