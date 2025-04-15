package org.start2do.util.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.start2do.util.spring.dto.JSON;


@Slf4j
@Import({UtilConfig.class, LogAopConfig.class})
@ConditionalOnProperty(prefix = "start2do.util", value = "enable", havingValue = "true")
public class UtilAutoConfig {

    @Bean
    @ConditionalOnMissingBean(ILogConfigBean.class)
    public ILogConfigBean logConfigBean() {
        return () -> new SimpleFilterProvider().addFilter("password_filter",
            SimpleBeanPropertyFilter.serializeAllExcept("password"));
    }

    @Bean
    @ConditionalOnMissingBean(JSON.class)
    public JSON json(LogAopConfig logAopConfig, ObjectMapper objectMapper) {
        return object -> {
            try {
                String json = objectMapper.writeValueAsString(object);
                int maxLength = logAopConfig.getMaxLogLength() != null ? logAopConfig.getMaxLogLength() : 2000;
                if (json.length() > maxLength) {
                    return json.substring(0, maxLength) + "...";
                }
                return json;
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                return e.getMessage();
            }
        };
    }
}



