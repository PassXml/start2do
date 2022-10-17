package org.start2do.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExcelUtil {


    @SneakyThrows
    public String toCsv(List list, Class clazz) {
        StringJoiner joiner = new StringJoiner("\r\n");
        StringJoiner titleStr = new StringJoiner(",");
        LinkedList<Field> fields = new LinkedList<>();
        for (Field field : clazz.getDeclaredFields()) {
            ExcelSetting setting = field.getDeclaredAnnotation(ExcelSetting.class);
            String title = null;
            if (setting == null) {
                continue;
            } else {
                if (setting.skin()) {
                    continue;
                }
                title = setting.value();

            }
            field.setAccessible(true);
            titleStr.add(title);
            fields.add(field);
        }
        joiner.add(titleStr.toString());
        for (Object o : list) {
            StringJoiner line = new StringJoiner(",");
            for (Field field : fields) {
                Object obj = field.get(o);
                if (obj == null) {
                    line.add("");
                } else {
                    line.add(obj.toString());
                }
            }
            joiner.add(line.toString());
        }
        return joiner.toString();
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExcelSetting {

        String value();

        boolean skin() default false;
    }
}
