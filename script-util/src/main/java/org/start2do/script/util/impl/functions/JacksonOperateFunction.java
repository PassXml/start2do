package org.start2do.script.util.impl.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JacksonOperateFunction {

    public static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static JsonNode toJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, JsonNode.class);
    }

    public static String toString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static String getText(JsonNode jsonNode, String key, String defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asText(defaultValue);
    }

    public static boolean getBoolean(JsonNode jsonNode, String key, boolean defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asBoolean(defaultValue);
    }

    public static Integer getInt(JsonNode jsonNode, String key, Integer defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asInt(defaultValue);
    }

    public static Long getLong(JsonNode jsonNode, String key, Long defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asLong(defaultValue);
    }

    public static Double getDouble(JsonNode jsonNode, String key, Double defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asDouble(defaultValue);
    }

}
