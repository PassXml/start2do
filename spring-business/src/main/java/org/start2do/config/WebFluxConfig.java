package org.start2do.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.start2do.util.StringCodeToEnumConverterFactory;

@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class WebFluxConfig implements WebFluxConfigurer {

    private final StringCodeToEnumConverterFactory stringCodeToEnumConverterFactory;
    private final Converter<String, LocalDate> localDateConverter;
    private final Converter<String, LocalDateTime> localDateTimeConverter;
    private final ObjectMapper mapper;

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().enableLoggingRequestDetails(true);
        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(stringCodeToEnumConverterFactory);
        registry.addConverter(localDateConverter);
        registry.addConverter(localDateTimeConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static").addResourceLocations("classpath:/static/");
    }
}
