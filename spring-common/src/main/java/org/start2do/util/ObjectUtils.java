package org.start2do.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectUtils {

    public <T> T getDefaultValue(T v1, T defaultValue, Function<T, Boolean> isNullFunction) {
        if (isNullFunction.apply(v1)) {
            return defaultValue;
        } else {
            return v1;
        }
    }

    public <T> T getDefaultValue(T v1, T defaultValue) {
        return getDefaultValue(v1, defaultValue, Objects::isNull);
    }

    public static <T, E, R> Boolean eq(T o1, E o2,
        boolean checkNull, List<Pairs> pairs) {
        List<Boolean> result = new ArrayList<>();
        for (Pairs<T, E, R> pair : pairs) {
            Eq<R> test = pair.test;
            R t = o1 == null ? null : pair.v1.apply(o1);
            R r = o2 == null ? null : pair.v2.apply(o2);
            if (checkNull) {
                if (t == null && r == null) {
                    result.add(true);
                    continue;
                } else if (t == null || r == null) {
                    result.add(false);
                    continue;
                }
            } else {
                if (t == null || r == null) {
                    result.add(true);
                    continue;
                }
            }
            result.add(result.add(test.test(t, r)));
        }
        return result.stream().filter(aBoolean -> aBoolean).count() == result.size();
    }


    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Pairs<T, E, R> {

        private Function<T, R> v1;
        private Function<E, R> v2;
        private Eq<R> test;

        public Pairs(Function<T, R> v1, Function<E, R> v2, Eq<R> test) {
            this.v1 = v1;
            this.v2 = v2;
            this.test = test;
        }

    }

    public interface Eq<T> {

        boolean test(T v1, T v2);
    }
}
