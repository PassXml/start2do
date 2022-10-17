package org.start2do.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectUtils {

    public static <T, E, R> Boolean eq(T o1, E o2,
        boolean checkNull, List<Pairs> pairs) {
        List<Boolean> result = new ArrayList<>();
        for (Pairs<T, E, R> pair : pairs) {
            Eq<R> test = pair.test;
            R t = pair.v1.apply(o1);
            R r = pair.v2.apply(o2);
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


    @Accessors(chain = true)
    @NoArgsConstructor
    public class Pairs<T, E, R> {

        private Function<T, R> v1;
        private Function<E, R> v2;
        private Eq<R> test;

        public Pairs(Function<T, R> v1, Function<E, R> v2, Eq<R> test) {
            this.v1 = v1;
            this.v2 = v2;
            this.test = test;
        }

        public Function<T, R> getV1() {
            return v1;
        }

        public Function<E, R> getV2() {
            return v2;
        }

        public Eq<R> getTest() {
            return test;
        }
    }

    public interface Eq<T> {

        boolean test(T v1, T v2);
    }
}
