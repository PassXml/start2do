package org.start2do.ebean;

import io.ebean.PagedList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.start2do.dto.Page;


public class EPage<T> extends Page<T> {

    public EPage(PagedList<T> result, Page page) {
        super(result.getTotalCount(), page.getSize(), page.getCurrent(), result.getList());
    }

    public <S> EPage(PagedList<S> result, Page page, Function<? super S, ? extends T> mapper) {
        super(result.getTotalCount(), page.getSize(), page.getCurrent(),
            result.getList() != null ? result.getList().stream().map(mapper).collect(Collectors.toList()) : null);
    }

}
