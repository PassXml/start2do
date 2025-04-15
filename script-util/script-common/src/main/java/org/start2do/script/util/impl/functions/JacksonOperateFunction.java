package org.start2do.script.util.impl.functions;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JacksonOperateFunction {

    public ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public JsonNode toJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, JsonNode.class);
    }

    public Map<String, Object> toMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }

    public Map<String, Object> toMap(String obj) throws JsonProcessingException {
        return objectMapper.readValue(obj, Map.class);
    }


    public JsonNode toJson(Map map) {
        return objectMapper.convertValue(map, JsonNode.class);
    }

    public JsonNode toJson(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream, JsonNode.class);
    }

    public JsonNode at(JsonNode jsonNode, String pointer) {
        return jsonNode.at(pointer);
    }

    public String toString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public String getText(JsonNode jsonNode, String key, String defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asText(defaultValue);
    }

    public String atString(String jsonStr, String path, String defaultValue) throws JsonProcessingException {
        if (jsonStr == null || path == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.at(JsonPointer.compile(path)).asText(defaultValue);
    }

    public String getString(JsonNode jsonNode, String key, String defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asText(defaultValue);
    }

    public String getString(String jsonStr, String key, String defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.get(key).asText(defaultValue);
    }

    public boolean getBoolean(JsonNode jsonNode, String key, boolean defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asBoolean(defaultValue);
    }

    public boolean getBoolean(String jsonStr, String key, Boolean defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.get(key).asBoolean(defaultValue);
    }

    public Integer getInt(JsonNode jsonNode, String key, Integer defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asInt(defaultValue);
    }

    public int getInt(String jsonStr, String key, Integer defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.get(key).asInt(defaultValue);
    }

    public Integer atInt(String jsonStr, String path, Integer defaultValue) throws JsonProcessingException {
        if (jsonStr == null || path == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.at(JsonPointer.compile(path)).asInt(defaultValue);
    }

    public Long getLong(JsonNode jsonNode, String key, Long defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asLong(defaultValue);
    }

    public Long getLong(String jsonStr, String key, Long defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.get(key).asLong(defaultValue);
    }

    public Long atLong(String jsonStr, String path, Long defaultValue) throws JsonProcessingException {
        if (jsonStr == null || path == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.at(JsonPointer.compile(path)).asLong(defaultValue);
    }

    public Double getDouble(JsonNode jsonNode, String key, Double defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        return jsonNode.get(key).asDouble(defaultValue);
    }

    public Double getDouble(String jsonStr, String key, Double defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        JsonNode json = toJson(jsonStr);
        return json.get(key).asDouble(defaultValue);
    }

    public static byte[] toBytes(Object consoleInfo) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(consoleInfo);
    }
}
