package org.start2do.util.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;


@Import(LogAopConfig.class)
@ConditionalOnProperty(prefix = "start2do.log", value = "enable", havingValue = "true")
public class LogAopAutoConfig {

    @Bean
    @ConditionalOnMissingBean(LogAop.JSON.class)
    public LogAop.JSON json(ObjectMapper objectMapper) {
        return object -> {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        };
    }

}
