package org.start2do.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.start2do.ebean.dict.IDictItem;

//@Component
public class StringToEnumConverter<T extends IDictItem> implements Converter<String, T> {

    private Map<String, T> enumMap = new HashMap<>();

    public StringToEnumConverter(Class<T> enumType) {
        T[] enums = enumType.getEnumConstants();
        for (T e : enums) {
            enumMap.put(e.getValue(), e);
        }
        for (T item : enums) {
            if (item instanceof Enum<?>) {
                enumMap.put(((Enum<?>) item).name(), item);
            }
        }
    }

    @Override
    public T convert(String source) {
        T t = enumMap.get(source);
        if (Objects.isNull(t)) {
            throw new IllegalArgumentException("无法匹配对应的枚举类型");
        }
        return t;
    }
}
