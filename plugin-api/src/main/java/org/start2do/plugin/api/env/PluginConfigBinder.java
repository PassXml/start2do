package org.start2do.plugin.api.env;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 插件侧配置绑定工具：把插件配置快照绑定成一个配置对象（非 Spring Bean）。
 * <p>
 * 典型用法：在 PF4J {@code Plugin#start()} 中初始化配置对象，而不依赖 Spring 容器。
 * <p>
 * 支持能力（轻量版）：
 * <ul>
 *   <li>从 {@code @ConfigurationProperties(prefix=...)} 解析 prefix（若无则可传入 override）；</li>
 *   <li>按 {@code prefix + "." + 字段名} 读取配置并注入字段；</li>
 *   <li>支持简单的“宽松匹配”：kebab/snake 与 camelCase 自动映射；</li>
 *   <li>支持点号嵌套 key（例如 {@code demo.sub.timeout}）绑定到嵌套对象；</li>
 *   <li>支持 List/Map（例如 {@code items[0].name}、{@code settings.timeout}、{@code settings[foo].timeout}）。</li>
 * </ul>
 * <p>
 * 说明：
 * - 这是“启动阶段可用”的最小依赖方案，不等价于 Spring Boot Binder 的完整能力；
 * - 建议配置对象使用无参构造函数，并尽量使用基础类型/包装类/String/enum。
 */
public final class PluginConfigBinder {

    private PluginConfigBinder() {
    }

    public static <T> T bindPluginOnly(String pluginId, Class<T> configClass) {
        return bindPluginOnly(pluginId, configClass, null);
    }

    public static <T> T bindPluginOnly(String pluginId, Class<T> configClass, String prefixOverride) {
        String prefix = normalizePrefix(prefixOverride);
        if (prefix == null) {
            prefix = normalizePrefix(resolveConfigurationPropertiesPrefix(configClass));
        }
        if (prefix == null) {
            throw new IllegalArgumentException("未找到 @ConfigurationProperties(prefix=...)，且未提供 prefixOverride");
        }

        T target = newInstance(configClass);
        Map<String, Object> snapshot = PluginConfigAccess.snapshotPluginOnly(pluginId, prefix);
        bindFromFlatSnapshot(target, snapshot);
        return target;
    }

    public static <T> T bindMergedPreferHost(String pluginId, Class<T> configClass) {
        return bindMergedPreferHost(pluginId, configClass, null);
    }

    public static <T> T bindMergedPreferHost(String pluginId, Class<T> configClass, String prefixOverride) {
        String prefix = normalizePrefix(prefixOverride);
        if (prefix == null) {
            prefix = normalizePrefix(resolveConfigurationPropertiesPrefix(configClass));
        }
        if (prefix == null) {
            throw new IllegalArgumentException("未找到 @ConfigurationProperties(prefix=...)，且未提供 prefixOverride");
        }

        T target = newInstance(configClass);

        // 用插件快照作为 key 列表来源，然后逐 key 走“宿主优先”视图读取实际值
        Map<String, Object> keys = PluginConfigAccess.snapshotPluginOnly(pluginId, prefix);
        if (keys == null || keys.isEmpty()) {
            return target;
        }

        Map<String, Object> merged = new LinkedHashMap<>();
        for (String k : keys.keySet()) {
            Object v = PluginConfigAccess.getMergedPreferHost(pluginId, prefix, k);
            if (v != null) {
                merged.put(k, v);
            }
        }
        bindFromFlatSnapshot(target, merged);
        return target;
    }

    private static <T> void bindFromFlatSnapshot(T target, Map<String, Object> snapshot) {
        if (target == null || snapshot == null || snapshot.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> e : snapshot.entrySet()) {
            String key = e.getKey();
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            applyProperty(target, key.trim(), e.getValue());
        }
    }

    private static void applyProperty(Object target, String key, Object value) {
        if (target == null || key == null) {
            return;
        }
        List<PathToken> tokens = parsePathTokens(key);
        if (tokens.isEmpty()) {
            return;
        }

        Object cur = target;
        for (int i = 0; i < tokens.size(); i++) {
            PathToken token = tokens.get(i);
            if (token == null || token.name == null || token.name.isEmpty()) {
                return;
            }

            boolean last = i == tokens.size() - 1;
            Field f = findField(cur.getClass(), token.name);
            if (f == null || isStaticOrFinal(f)) {
                return;
            }
            makeAccessible(f);

            Class<?> fieldType = f.getType();

            // 1) Map：支持 map[foo].bar 与 map.foo.bar 两种写法
            if (Map.class.isAssignableFrom(fieldType)) {
                Map<Object, Object> map = getOrCreateMap(cur, f);
                if (map == null) {
                    return;
                }

                Object mapKey = token.mapKey;
                if (mapKey == null) {
                    if (last) {
                        return;
                    }
                    PathToken mapKeyToken = tokens.get(i + 1);
                    if (mapKeyToken == null || mapKeyToken.name == null || mapKeyToken.name.trim().isEmpty()) {
                        return;
                    }
                    mapKey = mapKeyToken.name;
                    // 消费掉 map key token
                    i++;
                    last = i == tokens.size() - 1;
                }

                Class<?> mapValueType = resolveMapValueType(f);
                if (last) {
                    Object converted = convertValue(value, mapValueType == null ? Object.class : mapValueType);
                    if (converted == null && mapValueType != null && mapValueType.isPrimitive()) {
                        return;
                    }
                    map.put(mapKey, converted);
                    return;
                }

                Object next = map.get(mapKey);
                if (next == null) {
                    next = tryNewInstance(mapValueType);
                    if (next == null) {
                        return;
                    }
                    map.put(mapKey, next);
                }
                cur = next;
                continue;
            }

            // 2) List：支持 items[0].name
            if (List.class.isAssignableFrom(fieldType)) {
                if (token.index == null) {
                    // 为避免误绑定，List 必须显式带 [index]
                    return;
                }
                if (token.index < 0) {
                    return;
                }

                List<Object> list = getOrCreateList(cur, f);
                if (list == null) {
                    return;
                }
                ensureListSize(list, token.index);

                Class<?> elementType = resolveListElementType(f);
                if (last) {
                    Object converted = convertValue(value, elementType == null ? Object.class : elementType);
                    if (converted == null && elementType != null && elementType.isPrimitive()) {
                        return;
                    }
                    list.set(token.index, converted);
                    return;
                }

                Object element = list.get(token.index);
                if (element == null) {
                    element = tryNewInstance(elementType);
                    if (element == null) {
                        return;
                    }
                    list.set(token.index, element);
                }
                cur = element;
                continue;
            }

            // 3) 普通字段：若出现 [index]/[key]，当前轻量实现不支持（避免误绑定）
            if (token.index != null || token.mapKey != null) {
                return;
            }

            // 4) 标量：最后一个 token 直接赋值
            if (last) {
                Object converted = convertValue(value, fieldType);
                if (converted == null && fieldType.isPrimitive()) {
                    return;
                }
                try {
                    f.set(cur, converted);
                } catch (Exception ignore) {
                    return;
                }
                return;
            }

            // 5) 嵌套对象：确保中间对象存在
            Object next;
            try {
                next = f.get(cur);
            } catch (Exception ex) {
                return;
            }
            if (next == null) {
                next = tryNewInstance(fieldType);
                if (next == null) {
                    return;
                }
                try {
                    f.set(cur, next);
                } catch (Exception ex) {
                    return;
                }
            }
            cur = next;
        }
    }

    private static Map<Object, Object> getOrCreateMap(Object owner, Field f) {
        try {
            Object cur = f.get(owner);
            if (cur instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> map = (Map<Object, Object>) cur;
                return map;
            }
            Map<Object, Object> map = new LinkedHashMap<>();
            f.set(owner, map);
            return map;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static List<Object> getOrCreateList(Object owner, Field f) {
        try {
            Object cur = f.get(owner);
            if (cur instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) cur;
                return list;
            }
            List<Object> list = new ArrayList<>();
            f.set(owner, list);
            return list;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static void ensureListSize(List<Object> list, int index) {
        while (list.size() <= index) {
            list.add(null);
        }
    }

    private static Class<?> resolveListElementType(Field f) {
        if (f == null) {
            return Object.class;
        }
        Type gt = f.getGenericType();
        if (!(gt instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] args = ((ParameterizedType) gt).getActualTypeArguments();
        if (args == null || args.length < 1) {
            return Object.class;
        }
        return resolveToClass(args[0]);
    }

    private static Class<?> resolveMapValueType(Field f) {
        if (f == null) {
            return Object.class;
        }
        Type gt = f.getGenericType();
        if (!(gt instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] args = ((ParameterizedType) gt).getActualTypeArguments();
        if (args == null || args.length < 2) {
            return Object.class;
        }
        return resolveToClass(args[1]);
    }

    private static Class<?> resolveToClass(Type t) {
        if (t instanceof Class) {
            return (Class<?>) t;
        }
        if (t instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) t).getRawType();
            if (raw instanceof Class) {
                return (Class<?>) raw;
            }
        }
        return Object.class;
    }

    private static List<PathToken> parsePathTokens(String key) {
        String[] parts = key.split("\\.");
        List<PathToken> tokens = new ArrayList<>();
        for (String p : parts) {
            String part = p == null ? "" : p.trim();
            if (part.isEmpty()) {
                continue;
            }
            tokens.add(parseToken(part));
        }
        return tokens;
    }

    private static PathToken parseToken(String part) {
        int lb = part.indexOf('[');
        int rb = part.endsWith("]") ? part.lastIndexOf(']') : -1;
        if (lb >= 0 && rb > lb) {
            String name = part.substring(0, lb).trim();
            String inside = part.substring(lb + 1, rb).trim();
            if ((inside.startsWith("\"") && inside.endsWith("\"")) || (inside.startsWith("'") && inside.endsWith("'"))) {
                inside = inside.substring(1, inside.length() - 1).trim();
            }
            Integer idx = tryParseInt(inside);
            if (idx != null) {
                return new PathToken(name, idx, null);
            }
            return new PathToken(name, null, inside);
        }
        return new PathToken(part.trim(), null, null);
    }

    private static Integer tryParseInt(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        if (v.isEmpty()) {
            return null;
        }
        for (int i = 0; i < v.length(); i++) {
            char ch = v.charAt(i);
            if (i == 0 && ch == '-') {
                continue;
            }
            if (ch < '0' || ch > '9') {
                return null;
            }
        }
        try {
            return Integer.parseInt(v);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static final class PathToken {
        private final String name;
        private final Integer index;
        private final String mapKey;

        private PathToken(String name, Integer index, String mapKey) {
            this.name = name == null ? "" : name;
            this.index = index;
            this.mapKey = mapKey;
        }
    }

    private static Field findField(Class<?> type, String propertySegment) {
        if (type == null || propertySegment == null) {
            return null;
        }
        String want = normalizeName(propertySegment);
        if (want.isEmpty()) {
            return null;
        }

        for (Field f : getAllFields(type)) {
            String fieldName = f.getName();
            if (fieldName == null) {
                continue;
            }
            String fn = normalizeName(fieldName);
            if (want.equals(fn)) {
                return f;
            }
            // 兼容 boolean 常见命名：isEnabled 对应 key=enabled
            if (fn.startsWith("is") && fn.length() > 2) {
                String stripped = fn.substring(2);
                if (want.equals(stripped)) {
                    return f;
                }
            }
        }
        return null;
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> list = new ArrayList<>();
        Class<?> c = type;
        while (c != null && c != Object.class) {
            Field[] fields = c.getDeclaredFields();
            if (fields != null) {
                for (Field f : fields) {
                    list.add(f);
                }
            }
            c = c.getSuperclass();
        }
        return list;
    }

    private static boolean isStaticOrFinal(Field f) {
        int m = f.getModifiers();
        return Modifier.isStatic(m) || Modifier.isFinal(m);
    }

    private static void makeAccessible(Field f) {
        try {
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
        } catch (Exception ignore) {
        }
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '-' || ch == '_' || ch == ' ' || ch == '.') {
                continue;
            }
            sb.append(Character.toLowerCase(ch));
        }
        return sb.toString();
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (targetType == null) {
            return null;
        }
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }

        String s = String.valueOf(value).trim();

        // String
        if (targetType == String.class) {
            return s;
        }

        // boolean
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            String v = s.toLowerCase(Locale.ROOT);
            if ("true".equals(v) || "1".equals(v) || "yes".equals(v) || "y".equals(v) || "on".equals(v)) {
                return Boolean.TRUE;
            }
            if ("false".equals(v) || "0".equals(v) || "no".equals(v) || "n".equals(v) || "off".equals(v)) {
                return Boolean.FALSE;
            }
            return null;
        }

        // numbers
        if (targetType == int.class || targetType == Integer.class) {
            return toNumber(value, s, "int");
        }
        if (targetType == long.class || targetType == Long.class) {
            return toNumber(value, s, "long");
        }
        if (targetType == double.class || targetType == Double.class) {
            return toNumber(value, s, "double");
        }
        if (targetType == float.class || targetType == Float.class) {
            return toNumber(value, s, "float");
        }
        if (targetType == short.class || targetType == Short.class) {
            return toNumber(value, s, "short");
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return toNumber(value, s, "byte");
        }

        // enums
        if (targetType.isEnum()) {
            Object[] constants = targetType.getEnumConstants();
            if (constants == null) {
                return null;
            }
            for (Object c : constants) {
                if (c != null && s.equalsIgnoreCase(String.valueOf(c))) {
                    return c;
                }
            }
            // 兜底：按 name()
            for (Object c : constants) {
                if (c != null && s.equalsIgnoreCase(((Enum) c).name())) {
                    return c;
                }
            }
            return null;
        }

        // Map / List 等：尽量直接赋值（如果可赋值）
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // 对于集合/Map，轻量绑定不做深层转换（由上层的路径绑定逻辑负责逐项赋值）
        if (Map.class.isAssignableFrom(targetType) || List.class.isAssignableFrom(targetType)) {
            return null;
        }

        // 对于复杂类型，不做强行转换（建议用嵌套 key 绑定到嵌套对象）
        return null;
    }

    private static Object toNumber(Object value, String s, String kind) {
        try {
            if (value instanceof Number) {
                Number n = (Number) value;
                if ("int".equals(kind)) {
                    return n.intValue();
                }
                if ("long".equals(kind)) {
                    return n.longValue();
                }
                if ("double".equals(kind)) {
                    return n.doubleValue();
                }
                if ("float".equals(kind)) {
                    return n.floatValue();
                }
                if ("short".equals(kind)) {
                    return n.shortValue();
                }
                if ("byte".equals(kind)) {
                    return n.byteValue();
                }
            }
            if ("int".equals(kind)) {
                return Integer.parseInt(s);
            }
            if ("long".equals(kind)) {
                return Long.parseLong(s);
            }
            if ("double".equals(kind)) {
                return Double.parseDouble(s);
            }
            if ("float".equals(kind)) {
                return Float.parseFloat(s);
            }
            if ("short".equals(kind)) {
                return Short.parseShort(s);
            }
            if ("byte".equals(kind)) {
                return Byte.parseByte(s);
            }
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static <T> T newInstance(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("configClass 不能为空");
        }
        try {
            Constructor<T> c = type.getDeclaredConstructor();
            if (!c.isAccessible()) {
                c.setAccessible(true);
            }
            return c.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("配置类必须具备无参构造函数: " + type.getName(), e);
        }
    }

    private static Object tryNewInstance(Class<?> type) {
        if (type == null) {
            return null;
        }
        // 常见无法实例化类型直接跳过
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return null;
        }
        try {
            Constructor<?> c = type.getDeclaredConstructor();
            if (!c.isAccessible()) {
                c.setAccessible(true);
            }
            return c.newInstance();
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String resolveConfigurationPropertiesPrefix(Class<?> configClass) {
        if (configClass == null) {
            return null;
        }
        try {
            ClassLoader cl = configClass.getClassLoader();
            Class<?> annType = Class.forName("org.springframework.boot.context.properties.ConfigurationProperties", false, cl);
            if (!annType.isAnnotation()) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> t = (Class<? extends Annotation>) annType;
            Annotation ann = configClass.getAnnotation(t);
            if (ann == null) {
                return null;
            }

            // 优先 prefix()，其次 value()（兼容旧别名）
            String prefix = invokeStringMethod(ann, "prefix");
            if (prefix != null && !prefix.trim().isEmpty()) {
                return prefix.trim();
            }
            String value = invokeStringMethod(ann, "value");
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
            return null;
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static String invokeStringMethod(Object target, String method) {
        if (target == null || method == null) {
            return null;
        }
        try {
            Method m = target.getClass().getMethod(method);
            Object v = m.invoke(target);
            return v == null ? null : String.valueOf(v);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null) {
            return null;
        }
        String p = prefix.trim();
        if (p.isEmpty()) {
            return null;
        }
        while (p.endsWith(".")) {
            p = p.substring(0, p.length() - 1);
        }
        return p.isEmpty() ? null : p;
    }
}
