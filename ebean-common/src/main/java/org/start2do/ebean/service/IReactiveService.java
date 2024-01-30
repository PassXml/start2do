package org.start2do.ebean.service;

import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.typequery.TQRootBean;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.start2do.dto.Page;
import org.start2do.ebean.service.AbsService.Runner;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface IReactiveService<T extends Model> {

    String TokenKey = "_TOKEN_KEY_";
    String TransactionKey = "_Transaction_";


    Mono<T> findOneById(Object id);


    Mono<T> findOneByIdUseCache(Object id);

    Mono<T> update(T entity);

    Mono<Tuple2<Optional<Transaction>, Boolean>> delete(T obj);

    Mono<Boolean> deleteById(Object id);

    Mono<Boolean> handDeleteById(Object id);

    <S extends TQRootBean> Mono<Tuple2<Optional<Transaction>, Boolean>> handDelete(TQRootBean<T, S> bean);

    Mono<T> save(T entity);

    <S extends TQRootBean> Mono<T> getOne(TQRootBean<T, S> bean);

    Mono<T> getById(Object id);

    Mono<T> getByIdUseCache(Object id);

    Mono<List<T>> findAll();

    <S extends TQRootBean> Mono<T> findOne(TQRootBean<T, S> bean);

    <S extends TQRootBean> Mono<T> findOneUseCache(TQRootBean<T, S> bean);

    <S extends TQRootBean> Mono<List<T>> findAll(TQRootBean<T, S> bean);

    <S extends TQRootBean> Mono<List<T>> findAllUseCache(TQRootBean<T, S> bean);

    <S extends TQRootBean> Mono<Boolean> delete(TQRootBean<T, S> bean);

    <S extends TQRootBean> Mono<Page<T>> page(TQRootBean<T, S> bean, Page page);

    <S extends TQRootBean> Mono<Page<T>> pageUseCache(TQRootBean<T, S> bean, Page page);

    <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends TQRootBean, R> Mono<? extends Page<? extends R>> pageUseCache(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    //    public <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page, Consumer<Collection<T>> function,
//        Function<? super T, ? extends R> mapper)
    <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper);

    <S extends TQRootBean> Mono<Integer> count(TQRootBean<T, S> bean);

    <S extends TQRootBean> Mono<Integer> countUseCache(TQRootBean<T, S> bean);

    <S> Mono<Boolean> exists(TQRootBean<T, S> bean);

    <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2);

    <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2);
}
