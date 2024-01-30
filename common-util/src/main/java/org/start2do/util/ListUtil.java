package org.start2do.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ListUtil {

    /**
     * 拆分后运行
     */
    public <T> void splitAfterRun(int maxSize, List<T> list, Runner1<List<T>> spFunction) {
        splitAfterRun(maxSize, list, null, spFunction);
    }

    /**
     * 拆分后运行
     */
    public <T> void splitAfterRun(int maxSize, List<T> list, Function<Stream<T>, Stream<T>> streamFunction,
        Runner1<List<T>> spFunction) {
        if (list == null || maxSize < 0 || spFunction == null) {
            return;
        }
        Spliterators.spliterator(list, Spliterator.IMMUTABLE | Spliterator.ORDERED);
        int cur = 0;
        int end = list.size();
        while (cur < end) {
            log.debug("拆分运行,{},{}", maxSize, cur);
            int step = Math.min(maxSize, end - cur);
            List<T> subList = list.subList(cur, cur + step);
            if (streamFunction != null) {
                subList = streamFunction.apply(subList.stream()).collect(Collectors.toList());
            }
            spFunction.run(subList);
            cur += step;
        }
    }

    public <T, R> void diff(List<T> addSourceList, List<R> sourceList, Compare<T, R> eq, Consumer<List<T>> addFunction,
        Consumer<List<EqValue<T, R>>> updateFunction, Consumer<List<R>> removeFunction) {
        DiffDTO<T, R> diff = diff(addSourceList, sourceList, eq);
        if (addFunction != null) {
            addFunction.accept(diff.getAddList());
        }
        if (updateFunction != null) {
            updateFunction.accept(diff.getEqValues());
        }
        if (removeFunction != null) {
            removeFunction.accept(diff.getRemoveList());
        }
    }

    public <T, R> DiffDTO<T, R> diff(List<T> addSourceList, List<R> sourceList, Compare<T, R> eq) {
        List<T> addList = new ArrayList<>();
        List<EqValue<T, R>> eqList = new ArrayList<>();
        List<R> removeList = new ArrayList<>();
        if (addSourceList == null) {
            removeList.addAll(sourceList);
        } else {
            for (T t : addSourceList) {
                boolean isAdd = true;
                for (R r : sourceList) {
                    //相等,那么不需要添加
                    if (eq.test(t, r)) {
                        isAdd = false;
                        eqList.add(new EqValue<>(t, r));
                    }
                }
                if (isAdd) {
                    addList.add(t);
                }
            }
        }
        if (sourceList == null) {
            addList.addAll(addSourceList);
        } else {
            for (R r : sourceList) {
                boolean remove = true;
                for (T t : addSourceList) {
                    if (eq.test(t, r)) {
                        remove = false;
                    }
                }
                if (remove) {
                    removeList.add(r);
                }
            }
        }
        return new DiffDTO<T, R>(
            addList, eqList, removeList
        );
    }

    public <T, R> void fillInValue(List<T> source, List<R> items, Compare<T, R> compare, Runner<T, R> runner) {
        for (T t : source) {
            for (R item : items) {
                if (compare.test(t, item)) {
                    runner.run(t, item);
                }
            }
        }
    }

    public <T, R> void fillIn(List<T> source, List<R> items, Compare<T, R> compare, Runner<T, List<R>> runner) {
        for (T t : source) {
            List<R> fillInList = new ArrayList<>();
            for (R item : items) {
                if (compare.test(t, item)) {
                    fillInList.add(item);
                }
            }
            runner.run(t, fillInList);
        }
    }

    public static <T, R> R getValue(List<T> list, Predicate<T> predicate, Function<T, R> get, R defaultValue) {
        if (list != null) {
            for (T t : list) {
                if (predicate.test(t)) {
                    return get.apply(t);
                }
            }
        }
        return defaultValue;
    }

    public static <T, R> List<R> getValues(List<T> list, Predicate<T> predicate, Function<T, R> get) {
        List<R> result = new ArrayList<>();
        if (list != null) {
            for (T t : list) {
                if (predicate.test(t)) {
                    result.add(get.apply(t));
                }
            }
        }
        return result;
    }

    public static <T, R> Optional<T> findFirst(Collection<T> list, Function<T, R> get, R eqValue) {
        return list.stream().filter(t -> Objects.equals(get.apply(t), eqValue)).findFirst();
    }

    public static <T, R> T findFirst(Collection<T> list, T defaultValue, Function<T, R> get, R eqValue) {
        return findFirst(list, get, eqValue).orElseGet(() -> defaultValue);
    }

    public static <T, R, Z> Z findFirst(Collection<T> list, Z defaultValue, Function<T, R> get, R eqValue,
        Function<T, Z> function) {
        return findFirst(list, get, eqValue).map(function).orElseGet(() -> defaultValue);
    }

    public static <T, Z, R> Z findFirstThrow(Collection<T> list, Supplier<RuntimeException> exception,
        Function<T, R> get,
        R eqValue,
        Function<T, Z> function) {
        return findFirst(list, get, eqValue).map(function).orElseThrow(exception);
    }

    public static boolean isEmpty(List req) {
        return req == null || req.size() == 0;
    }


    public interface Compare<T, R> {

        boolean test(T t, R r);
    }

    public interface Runner<T, R> {

        void run(T t, R r);
    }

    public interface Runner1<T> {

        void run(T spList);
    }


    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class EqValue<T, R> {

        private T p1;
        private R p2;

        public EqValue(T add, R source) {
            this.p1 = add;
            this.p2 = source;
        }
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class DiffDTO<T, R> {

        private List<T> addList;
        private List<EqValue<T, R>> eqValues;
        private List<R> removeList;

        public DiffDTO(List<T> addList, List<EqValue<T, R>> eqValues, List<R> removeList) {
            this.addList = addList;
            this.eqValues = eqValues;
            this.removeList = removeList;
        }
    }


    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
