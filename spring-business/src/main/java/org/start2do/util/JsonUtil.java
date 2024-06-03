package org.start2do.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.start2do.util.spring.SpringBeanUtil;

@Slf4j
@Component
public class JsonUtil implements CommandLineRunner {

    public static ObjectMapper objectMapper;

    public static byte[] toJsonStr(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String s, Class<T> aclass) {
        try {
            return objectMapper.readValue(s, aclass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        this.objectMapper = SpringBeanUtil.getBean(ObjectMapper.class);
    }
}
