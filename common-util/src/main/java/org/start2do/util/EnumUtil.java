package org.start2do.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import lombok.experimental.UtilityClass;
import org.start2do.ebean.dict.IDictItem;

/**
 * @author hello
 */
@UtilityClass
public class EnumUtil {

    public interface IEnum {

        String getCode();
    }

    /**
     * str编码转化成枚举值
     */
    public <T extends IEnum> List<T> code2enum(T[] values, String s) {
        if (StringUtils.isEmpty(s)) {
            return List.of();
        }
        if (values == null || values.length < 1) {
            return List.of();
        }
        List<T> enums = new ArrayList<>();
        Integer integer = Integer.valueOf(s, 2);
        for (T value : values) {
            int i = Integer.parseInt(value.getCode(), 2) & integer;
            if (i > 0) {
                enums.add(value);
            }
        }
        return enums;
    }

    /**
     * 转化成数据库类型
     */
    public <T extends IEnum> String enum2code(String baseStr, List<T> enums) {
        if (StringUtils.isEmpty(baseStr)) {
            return null;
        }
        if (enums == null || enums.isEmpty()) {
            return baseStr;
        }
        Integer i = Integer.valueOf(baseStr, 2);
        for (T s : enums) {
            i = i | Integer.valueOf(s.getCode(), 2);
        }
        String string = Integer.toBinaryString(i);
        int lengths = baseStr.length() - string.length();
        StringBuilder stringBuilder = new StringBuilder(string);
        for (int p = 0; p < lengths; p++) {
            stringBuilder.insert(0, "0");
        }
        return stringBuilder.toString();
    }

    public <T extends IDictItem> String toStr(Collection<T> list, String split) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(split);
        for (T t : list) {
            joiner.add(t.getLabel());
        }
        return joiner.toString();
    }

    public <T extends IDictItem> String toStr(T t) {
        if (t == null) {
            return null;
        }
        return t.getLabel();
    }
}
