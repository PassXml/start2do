package org.start2do;


import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ComponentScans(value = {
    @ComponentScan(value = "org.start2do.controller"),
    @ComponentScan(value = "org.start2do.service"),
    @ComponentScan(value = "org.start2do.util"),
    @ComponentScan(value = "org.start2do.entity"),
})
@Import(BusinessConfig.class)
@ConditionalOnProperty(prefix = "start2do.business", value = "enable", havingValue = "true")
public class BusinessAutoConfig {


    /**
     * string转localdate
     */
    @Bean
    public Converter<String, LocalDate> localDateConverter(BusinessConfig config) {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                if (source.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(source);
                } catch (Exception e) {
                    return LocalDate.parse(source, DateTimeFormatter.ofPattern(config.getDatePattern()));
                }
            }
        };
    }

    /**
     * string转localdatetime
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter(BusinessConfig config) {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                if (source.trim().length() == 0) {
                    return null;
                }
                // 先尝试ISO格式: 2019-07-15T16:00:00
                try {
                    return LocalDateTime.parse(source);
                } catch (Exception e) {
                    return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(config.getDateTimePattern()));
                }
            }
        };
    }

    /**
     * 统一配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer(BusinessConfig config) {
        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
            DateTimeFormatter.ofPattern(config.getDateTimePattern()));
        module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
        return builder -> {
            builder.simpleDateFormat(config.getDateTimePattern());
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(config.getDatePattern())));
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(config.getDateTimePattern())));
            builder.modules(module);
        };
    }

    @Bean
    @ConditionalOnMissingBean(value = {PasswordEncoder.class})
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
