package org.start2do.ebean.service;

import io.ebean.Model;
import io.ebean.typequery.QueryBean;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.start2do.dto.Page;
import org.start2do.ebean.service.AbsService.Runner;

public interface IService<T extends Model> {


    T findOneById(Object id);

    T findOneByIdUseCache(Object id);

    void update(T entity);

    boolean delete(T obj);

    int deleteById(Object id);

    int handDeleteById(Object id);

    <S extends QueryBean<T,S>> boolean handDelete(QueryBean<T, S> bean);

    void save(T entity);

    <S extends QueryBean<T,S>> T getOne(QueryBean<T, S> bean);

    T getById(Object id);

    T getByIdUseCache(Object id);

    List<T> findAll();

    <S extends QueryBean<T,S>> T findOne(QueryBean<T, S> bean);

    <S extends QueryBean<T,S>> T findOneUseCache(QueryBean<T, S> bean);

    <S extends QueryBean<T,S>> List<T> findAll(QueryBean<T, S> bean);

    <S extends QueryBean<T,S>> List<T> findAllUseCache(QueryBean<T, S> bean);

    <S extends QueryBean<T,S>> void delete(QueryBean<T, S> bean);

    <S extends QueryBean<T,S>> Page<T> page(QueryBean<T, S> bean, Page page);

    <S extends QueryBean<T,S>> Page<T> pageUseCache(QueryBean<T, S> bean, Page page);

    <S extends QueryBean<T,S>, R> Page<R> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T,S>, R> Page<R> pageUseCache(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T,S>, R> Page<R> page(QueryBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T,S>> int count(QueryBean<T, S> bean);


    <S extends QueryBean<T,S>> int countUseCache(QueryBean<T, S> bean);

    <S extends QueryBean<T,S>> boolean exists(QueryBean<T, S> bean);

    <S extends QueryBean<T,S>, R> Page<R> page(QueryBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2);

    <S extends QueryBean<T,S>, R> Page<R> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2);


}
