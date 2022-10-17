package org.start2do.ebean;

import org.start2do.dto.Page;
import io.ebean.PagedList;
import java.util.function.Function;
import java.util.stream.Collectors;


public class EPage<T> extends Page<T> {

    public EPage(PagedList<T> result) {
        super(
            result.getTotalCount(), result.getPageSize(), result.getPageIndex(), result.getList()
        );
    }

    public <S> EPage(PagedList<S> result, Function<? super S, ? extends T> mapper) {
        super(
            result.getTotalCount(), result.getPageSize(), result.getPageIndex(),
            result.getList().stream().map(mapper).collect(
                Collectors.toList())
        );
    }
}
