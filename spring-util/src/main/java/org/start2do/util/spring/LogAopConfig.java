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
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "start2do")
public class LogAopConfig {

    @Setter
    private List<String> skinClass;
    @Getter
    private List<Class> skinClazz;

    @PostConstruct
    public void init() {
        if (skinClazz == null) {
            skinClazz = new ArrayList<>(
                Arrays.asList(
                    HttpServletRequest.class, HttpServletResponse.class,
                    ServletResponse.class, ServletRequest.class, OutputStream.class,
                    ByteArrayOutputStream.class
                )
            );
        }
        if (skinClass == null) {
            return;
        }
        for (String aClass : skinClass) {
            try {
                skinClazz.add(Class.forName(aClass));
            } catch (ClassNotFoundException e) {

            }
        }
    }
}
