package org.start2do.util;


import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

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
    public static <T extends IEnum> List<T> code2enum(T[] values, String s) {
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
    public static <T extends IEnum> String enum2code(String baseStr, List<T> enums) {
        Integer i = Integer.valueOf(baseStr, 2);
        if (enums == null || enums.isEmpty()) {
            return baseStr;
        }
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
}
