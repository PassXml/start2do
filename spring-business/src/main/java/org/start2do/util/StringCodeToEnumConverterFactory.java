package org.start2do.util;


import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.start2do.ebean.dict.IDictItem;

/**
 * <p>
 * 编码 -> 枚举 转化器工厂类
 * </p>
 */
@Component
public class StringCodeToEnumConverterFactory implements ConverterFactory<String, IDictItem> {

    protected static final Map<Class, Converter> CONVERTERS = new ConcurrentReferenceHashMap<>();

    /**
     * 获取一个从 Integer 转化为 T 的转换器，T 是一个泛型，有多个实现
     *
     * @param targetType 转换后的类型
     * @return 返回一个转化器
     */
    @Override
    public <T extends IDictItem> Converter<String, T> getConverter(Class<T> targetType) {
        Converter<String, T> converter = CONVERTERS.get(targetType);
        if (converter == null) {
            converter = new StringToEnumConverter<>(targetType);
            CONVERTERS.put(targetType, converter);
        }
        return converter;
    }
}
