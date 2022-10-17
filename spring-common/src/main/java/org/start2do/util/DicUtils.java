package org.start2do.util;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

public abstract class DicUtils {

    public static Function<String, HashMap<String, String>> GETFUNCTION = null;
    public static GetFunction2 GETFUNCTION2 = null;
    protected static HashMap<String, HashMap<String, String>> hashMap = new HashMap<>();

    public static String getLabelStr(String type, String key) {
        return Optional.ofNullable(hashMap.get(type)).map(stringStringHashMap -> stringStringHashMap.get(key))
            .orElse(key);
    }

    public static HashMap<String, String> get(String type) {
        return hashMap.getOrDefault(type, new HashMap<>());
    }

    public static String getLabelStr(String type, String attr_useStatus, String attr_useStatus_use) {
        return getLabelStr(String.join(":", type, attr_useStatus), attr_useStatus_use);
    }

    public interface GetFunction2 {

        String get(String type, String key);
    }
}
