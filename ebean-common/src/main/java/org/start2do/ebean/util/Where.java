package org.start2do.ebean.util;

import io.ebean.typequery.PEnum;
import io.ebean.typequery.PLocalDateTime;
import io.ebean.typequery.PString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.function.Consumer;
import org.start2do.util.StringUtils;

public class Where {

    private static final Where where = new Where();

    public static Where ready() {
        return where;
    }

    public <R> Where set(Boolean b, Consumer<R> function, R s) {
        if (b) {
            function.accept(s);
        }
        return where;
    }

    public <R> Where notNull(R s, Consumer<R> function) {
        if (s != null) {
            function.accept(s);
        }
        return where;
    }

    public <R> Where defaultValue(R s, R defaultValue, Consumer<R> function) {
        if (s != null) {
            function.accept(s);
        } else {
            function.accept(defaultValue);
        }
        return where;
    }


    public Where notEmpty(String s, Consumer<String> function) {
        if (StringUtils.isNotEmpty(s)) {
            function.accept(s);
        }
        return where;
    }

    public Where notEmpty(String s, String default_, Consumer<String> function) {
        if (StringUtils.isNotEmpty(s)) {
            function.accept(s);
        } else {
            function.accept(default_);
        }
        return where;
    }


    public <T> Where notEmpty(Collection<T> s, Consumer<Collection<T>> function) {
        if (s != null && !s.isEmpty()) {
            function.accept(s);
        }
        return where;
    }

    public <T> Where notEmpty(Collection<T> s, Collection<T> default_, Consumer<Collection<T>> function) {
        if (s != null && !s.isEmpty()) {
            function.accept(s);
        } else {
            function.accept(default_);
        }
        return where;
    }

    public Where like(String s, Consumer<String> function) {
        if (s != null && !s.isEmpty()) {
            function.accept("%" + s + "%");
        }
        return where;
    }

    public Where like(String s, PString function) {
        if (s != null && !s.isEmpty()) {
            function.like("%" + s + "%");
        }
        return where;
    }

    public Where between(PLocalDateTime patten, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            patten.between(startTime, endTime);
        }
        return where;
    }

    public Where between(PLocalDateTime patten, LocalDate startDate, LocalDate endTime) {
        if (startDate != null && endTime != null) {
            between(patten, LocalDateTime.of(startDate, LocalTime.of(0, 0)),
                LocalDateTime.of(startDate, LocalTime.of(59, 59, 59, 999)));
        }
        return where;
    }

    public Where notNull(Enum status,
        PEnum entityStatus) {
        if (status != null) {
            entityStatus.eq(status);
        }
        return where;
    }
}
