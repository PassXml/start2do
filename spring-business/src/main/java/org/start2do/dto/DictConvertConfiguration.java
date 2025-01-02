package org.start2do.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.start2do.util.StringCodeToEnumConverterFactory;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business", name = "enable-dict-convert", havingValue = "true")
public class DictConvertConfiguration implements WebMvcConfigurer {

    private final StringCodeToEnumConverterFactory stringCodeToEnumConverterFactory;

    /**
     * 枚举类的转换器工厂 addConverterFactory 适用于 requestParam情况
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(stringCodeToEnumConverterFactory);
    }
}
