package org.start2do.script.util.impl.functions;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JacksonOperateFunction {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final TypeReference<List<Object>> LIST_TYPE = new TypeReference<List<Object>>() {
    };

    public ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public JsonNode toJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, JsonNode.class);
    }

    public JsonNode toJson(Map map) {
        return objectMapper.convertValue(map, JsonNode.class);
    }

    public JsonNode toJson(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream, JsonNode.class);
    }

    public JsonNode toJsonNode(Object object) {
        return objectMapper.valueToTree(object);
    }

    public <T> T toObject(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    public <T> T toObject(JsonNode jsonNode, Class<T> clazz) throws JsonProcessingException {
        if (isNullOrMissing(jsonNode)) {
            return null;
        }
        return objectMapper.treeToValue(jsonNode, clazz);
    }

    public Map<String, Object> toMap(Object obj) {
        return objectMapper.convertValue(obj, MAP_TYPE);
    }

    public Map<String, Object> toMap(String obj) throws JsonProcessingException {
        return objectMapper.readValue(obj, MAP_TYPE);
    }

    public Map<String, Object> atMap(String jsonStr, String path) throws JsonProcessingException {
        JsonNode node = atNode(jsonStr, path);
        if (isNullOrMissing(node)) {
            return new HashMap<>();
        }
        return toMap(node);
    }

    public List<Object> toList(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, LIST_TYPE);
    }

    public List<Object> toList(JsonNode jsonNode) {
        if (isNullOrMissing(jsonNode)) {
            return new ArrayList<>();
        }
        return objectMapper.convertValue(jsonNode, LIST_TYPE);
    }

    public List<Object> getList(String jsonStr, String key) throws JsonProcessingException {
        if (jsonStr == null || key == null) {
            return new ArrayList<>();
        }
        return toList(getNode(toJson(jsonStr), key));
    }

    public JsonNode getArray(String jsonStr, String key) throws JsonProcessingException {
        JsonNode node = getNode(jsonStr, key);
        return isArray(node) ? node : MissingNode.getInstance();
    }

    public List<Object> atList(String jsonStr, String path) throws JsonProcessingException {
        if (jsonStr == null || path == null) {
            return new ArrayList<>();
        }
        return toList(atNode(jsonStr, path));
    }

    public JsonNode atArray(String jsonStr, String path) throws JsonProcessingException {
        JsonNode node = atNode(jsonStr, path);
        return isArray(node) ? node : MissingNode.getInstance();
    }

    public JsonNode at(JsonNode jsonNode, String pointer) {
        return atNode(jsonNode, pointer);
    }

    public JsonNode atNode(JsonNode jsonNode, String pointer) {
        if (jsonNode == null || pointer == null) {
            return MissingNode.getInstance();
        }
        return jsonNode.at(JsonPointer.compile(pointer));
    }

    public JsonNode atNode(String jsonStr, String path) throws JsonProcessingException {
        if (jsonStr == null || path == null) {
            return MissingNode.getInstance();
        }
        return atNode(toJson(jsonStr), path);
    }

    public JsonNode getNode(JsonNode jsonNode, String key) {
        if (jsonNode == null || key == null) {
            return MissingNode.getInstance();
        }
        JsonNode child = jsonNode.get(key);
        return child == null ? MissingNode.getInstance() : child;
    }

    public JsonNode getNode(String jsonStr, String key) throws JsonProcessingException {
        if (jsonStr == null || key == null) {
            return MissingNode.getInstance();
        }
        return getNode(toJson(jsonStr), key);
    }

    public boolean has(JsonNode jsonNode, String key) {
        return !isNullOrMissing(getNode(jsonNode, key));
    }

    public boolean has(String jsonStr, String key) throws JsonProcessingException {
        return !isNullOrMissing(getNode(jsonStr, key));
    }

    public boolean hasPath(String jsonStr, String path) throws JsonProcessingException {
        return !isNullOrMissing(atNode(jsonStr, path));
    }

    public boolean isArray(JsonNode jsonNode) {
        return jsonNode != null && jsonNode.isArray();
    }

    public int size(JsonNode jsonNode) {
        return jsonNode == null ? 0 : jsonNode.size();
    }

    public String toString(Object object) throws JsonProcessingException {
        return toJsonString(object);
    }

    public String toJsonString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public String toPrettyString(Object object) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public String toPrettyString(JsonNode jsonNode) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    public String getText(JsonNode jsonNode, String key, String defaultValue) {
        JsonNode child = getNode(jsonNode, key);
        return isNullOrMissing(child) ? defaultValue : child.asText(defaultValue);
    }

    public String atString(String jsonStr, String path, String defaultValue) throws JsonProcessingException {
        JsonNode node = atNode(jsonStr, path);
        return isNullOrMissing(node) ? defaultValue : node.asText(defaultValue);
    }

    public String getString(JsonNode jsonNode, String key, String defaultValue) {
        return getText(jsonNode, key, defaultValue);
    }

    public String getString(String jsonStr, String key, String defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        return getText(toJson(jsonStr), key, defaultValue);
    }

    public boolean getBoolean(JsonNode jsonNode, String key, boolean defaultValue) {
        JsonNode child = getNode(jsonNode, key);
        return isNullOrMissing(child) ? defaultValue : child.asBoolean(defaultValue);
    }

    public boolean getBoolean(String jsonStr, String key, Boolean defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        return getBoolean(toJson(jsonStr), key, defaultValue);
    }

    public boolean atBoolean(String jsonStr, String path, boolean defaultValue) throws JsonProcessingException {
        JsonNode node = atNode(jsonStr, path);
        return isNullOrMissing(node) ? defaultValue : node.asBoolean(defaultValue);
    }

    public Integer getInt(JsonNode jsonNode, String key, Integer defaultValue) {
        JsonNode child = getNode(jsonNode, key);
        return isNullOrMissing(child) ? defaultValue : child.asInt(defaultValue);
    }

    public int getInt(String jsonStr, String key, Integer defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        return getInt(toJson(jsonStr), key, defaultValue);
    }

    public Integer atInt(String jsonStr, String path, Integer defaultValue) throws JsonProcessingException {
        JsonNode node = atNode(jsonStr, path);
        return isNullOrMissing(node) ? defaultValue : node.asInt(defaultValue);
    }

    public Long getLong(JsonNode jsonNode, String key, Long defaultValue) {
        JsonNode child = getNode(jsonNode, key);
        return isNullOrMissing(child) ? defaultValue : child.asLong(defaultValue);
    }

    public Long getLong(String jsonStr, String key, Long defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        return getLong(toJson(jsonStr), key, defaultValue);
    }

    public Long atLong(String jsonStr, String path, Long defaultValue) throws JsonProcessingException {
        JsonNode node = atNode(jsonStr, path);
        return isNullOrMissing(node) ? defaultValue : node.asLong(defaultValue);
    }

    public Double getDouble(JsonNode jsonNode, String key, Double defaultValue) {
        JsonNode child = getNode(jsonNode, key);
        return isNullOrMissing(child) ? defaultValue : child.asDouble(defaultValue);
    }

    public Double getDouble(String jsonStr, String key, Double defaultValue) throws JsonProcessingException {
        if (jsonStr == null) {
            return defaultValue;
        }
        return getDouble(toJson(jsonStr), key, defaultValue);
    }

    public Double atDouble(String jsonStr, String path, Double defaultValue) throws JsonProcessingException {
        JsonNode node = atNode(jsonStr, path);
        return isNullOrMissing(node) ? defaultValue : node.asDouble(defaultValue);
    }

    public JsonNode set(JsonNode jsonNode, String path, Object value) {
        if (path == null) {
            return jsonNode;
        }
        JsonNode valueNode = value == null ? NullNode.getInstance() : toJsonNode(value);
        if (jsonNode == null || isNullOrMissing(jsonNode)) {
            if (path.isEmpty() || "/".equals(path)) {
                return valueNode;
            }
            jsonNode = objectMapper.createObjectNode();
        }
        if (path.isEmpty() || "/".equals(path)) {
            return valueNode;
        }
        PathRef pathRef = parsePath(path);
        if (pathRef == null) {
            return jsonNode;
        }
        JsonNode workingRoot = jsonNode.deepCopy();
        JsonNode parent = ensureParentNode(workingRoot, pathRef.parentPointer);
        setChildValue(parent, pathRef.leafToken, valueNode);
        return workingRoot;
    }

    public String set(String jsonStr, String path, Object value) throws JsonProcessingException {
        return toJsonString(set(jsonStr == null ? null : toJson(jsonStr), path, value));
    }

    public JsonNode remove(JsonNode jsonNode, String path) {
        if (jsonNode == null || path == null || path.isEmpty() || "/".equals(path)) {
            return jsonNode;
        }
        PathRef pathRef = parsePath(path);
        if (pathRef == null) {
            return jsonNode;
        }
        JsonNode copy = jsonNode.deepCopy();
        JsonNode parent = copy.at(pathRef.parentPointer);
        if (parent instanceof ObjectNode) {
            ((ObjectNode) parent).remove(pathRef.leafToken);
        } else if (parent instanceof ArrayNode) {
            Integer index = toArrayIndex(pathRef.leafToken);
            if (index != null && index >= 0 && index < parent.size()) {
                ((ArrayNode) parent).remove(index);
            }
        }
        return copy;
    }

    public String remove(String jsonStr, String path) throws JsonProcessingException {
        return toJsonString(remove(jsonStr == null ? null : toJson(jsonStr), path));
    }

    public JsonNode merge(JsonNode jsonNode, Map<String, Object> values) {
        ObjectNode target = jsonNode instanceof ObjectNode ? (ObjectNode) jsonNode.deepCopy() : objectMapper.createObjectNode();
        if (values == null || values.isEmpty()) {
            return target;
        }
        values.forEach((key, value) -> target.set(key, value == null ? NullNode.getInstance() : toJsonNode(value)));
        return target;
    }

    public String merge(String jsonStr, Map<String, Object> values) throws JsonProcessingException {
        return toJsonString(merge(jsonStr == null ? null : toJson(jsonStr), values));
    }

    public static byte[] toBytes(Object consoleInfo) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(consoleInfo);
    }

    private boolean isNullOrMissing(JsonNode jsonNode) {
        return jsonNode == null || jsonNode.isNull() || jsonNode.isMissingNode();
    }

    private PathRef parsePath(String path) {
        if (path == null || path.isEmpty() || !path.startsWith("/")) {
            return null;
        }
        int lastSlash = path.lastIndexOf('/');
        String parentPointer = lastSlash == 0 ? "" : path.substring(0, lastSlash);
        String leafToken = unescapePathToken(path.substring(lastSlash + 1));
        return new PathRef(parentPointer, leafToken);
    }

    private JsonNode ensureParentNode(JsonNode root, String parentPointer) {
        if (parentPointer == null || parentPointer.isEmpty()) {
            return root;
        }
        JsonNode current = root;
        String[] tokens = parentPointer.substring(1).split("/");
        for (String rawToken : tokens) {
            String token = unescapePathToken(rawToken);
            JsonNode next = current.get(token);
            if (!(next instanceof ObjectNode)) {
                next = objectMapper.createObjectNode();
                if (current instanceof ObjectNode) {
                    ((ObjectNode) current).set(token, next);
                }
            }
            current = next;
        }
        return current;
    }

    private void setChildValue(JsonNode parent, String leafToken, JsonNode valueNode) {
        if (parent instanceof ObjectNode) {
            ((ObjectNode) parent).set(leafToken, valueNode);
        } else if (parent instanceof ArrayNode) {
            Integer index = toArrayIndex(leafToken);
            if (index == null || index < 0) {
                return;
            }
            ArrayNode arrayNode = (ArrayNode) parent;
            while (arrayNode.size() <= index) {
                arrayNode.add(NullNode.getInstance());
            }
            arrayNode.set(index, valueNode);
        }
    }

    private Integer toArrayIndex(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String unescapePathToken(String token) {
        return token.replace("~1", "/").replace("~0", "~");
    }

    private static class PathRef {
        private final String parentPointer;
        private final String leafToken;

        private PathRef(String parentPointer, String leafToken) {
            this.parentPointer = parentPointer;
            this.leafToken = leafToken;
        }
    }
}
