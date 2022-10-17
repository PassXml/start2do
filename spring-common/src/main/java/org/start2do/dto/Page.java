package org.start2do.dto;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class Page<T> implements Serializable {

    /**
     * 总条数
     */
    protected long total;
    /**
     * 每页显示条数
     */
    protected int size = 10;
    /**
     * 当前页
     */
    protected int current = 1;
    /**
     * 集合
     */
    protected List<T> records;

    public Page() {
    }

    public Page(long total, int size, int current, List<T> records) {
        this.total = total;
        this.size = size;
        this.current = current;
        this.records = records;
    }

    public <S> Page(Page<S> tPage, Function<? super S, ? extends T> mapper) {
        this.total = tPage.getTotal();
        this.size = tPage.getSize();
        this.current = tPage.getCurrent();
        this.records = tPage.getRecords().stream().map(mapper).collect(Collectors.toList());
    }

    public <S> Page<S> map(Function<T, S> mapper) {
        Page<S> page = new Page<>(this, mapper);
        return page;
    }

    public Integer getOffset() {
        return Math.max(0, current - 1) * size;
    }

}
