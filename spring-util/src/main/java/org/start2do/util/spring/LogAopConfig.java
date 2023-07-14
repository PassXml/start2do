package org.start2do.util.spring;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;


@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
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
        skinClazz.addAll(Arrays.asList(HttpServletRequest.class, HttpServletResponse.class, ServletResponse.class,
            ServletRequest.class, OutputStream.class, ByteArrayOutputStream.class, MultipartFile.class));
    }


}
