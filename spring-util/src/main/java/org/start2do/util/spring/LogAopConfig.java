package org.start2do.util.spring;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ImportAutoConfiguration
@ConfigurationProperties(prefix = "start2do.log")
@ConditionalOnProperty(prefix = "start2do.log", value = "enable", havingValue = "true")
public class LogAopConfig {

    private boolean enable;
    private Integer maxLogLength;
    private List<Class> skinClazz;
    private String name;
    private Set<String> skipUrl;

    @PostConstruct
    public void init() {
        if (skinClazz == null) {
            skinClazz = new ArrayList<>();
        }
        if (skipUrl == null) {
            skipUrl = new HashSet<>();
        }
        skinClazz.addAll(Arrays.asList(OutputStream.class, ByteArrayOutputStream.class));
        String[] t = {"org.springframework.web.multipart.MultipartFile",
            "jakarta.servlet.ServletResponse", "jakarta.servlet.http.HttpServletResponse",
            "jakarta.servlet.http.HttpServletRequest", "jakarta.servlet.ServletRequest"};
        for (String classStr : t) {
            try {
                Class<?> aClass = Class.forName(classStr);
                skinClazz.add(aClass);
            } catch (ClassNotFoundException e) {

            }
        }

    }


}
