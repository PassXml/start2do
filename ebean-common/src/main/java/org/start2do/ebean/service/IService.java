package org.start2do.ebean.service;

import io.ebean.Model;
import io.ebean.typequery.TQRootBean;
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

    <S extends TQRootBean> boolean handDelete(TQRootBean<T, S> bean);

    void save(T entity);

    <S extends TQRootBean> T getOne(TQRootBean<T, S> bean);

    T getById(Object id);

    T getByIdUseCache(Object id);

    List<T> findAll();

    <S extends TQRootBean> T findOne(TQRootBean<T, S> bean);

    <S extends TQRootBean> T findOneUseCache(TQRootBean<T, S> bean);

    <S extends TQRootBean> List<T> findAll(TQRootBean<T, S> bean);

    <S extends TQRootBean> List<T> findAllUseCache(TQRootBean<T, S> bean);

    <S extends TQRootBean> void delete(TQRootBean<T, S> bean);

    <S extends TQRootBean> Page<T> page(TQRootBean<T, S> bean, Page page);

    <S extends TQRootBean> Page<T> pageUseCache(TQRootBean<T, S> bean, Page page);

    <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends TQRootBean, R> Page<R> pageUseCache(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper);

    <S extends TQRootBean> int count(TQRootBean<T, S> bean);


    <S extends TQRootBean> int countUseCache(TQRootBean<T, S> bean);

    <S> boolean exists(TQRootBean<T, S> bean);

    <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2);

    <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2);
}
