package org.start2do.ebean.service;

import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.typequery.QueryBean;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.start2do.dto.Page;
import org.start2do.ebean.service.AbsService.Runner;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface IMixService<T extends Model> {


    T findOneById(Object id);

    T findOneByIdUseCache(Object id);

    void update(T entity);

    void update(T entity, Transaction transaction);

    boolean delete(T obj);

    boolean delete(T obj, Transaction transaction);

    int deleteById(Object id);

    int handDeleteById(Object id);

    <S extends QueryBean<T, S>> boolean handDelete(QueryBean<T, S> bean);

    void save(T entity);

    <S extends QueryBean<T, S>> T getOne(QueryBean<T, S> bean);

    T getById(Object id);

    T getByIdUseCache(Object id);

    List<T> findAll();

    <S extends QueryBean<T, S>> T findOne(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> T findOneUseCache(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> List<T> findAll(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> List<T> findAllUseCache(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> void delete(QueryBean<T, S> bean);

    Mono<T> findOneByIdReactive(Object id);

    Mono<T> findOneByIdUseCacheReactive(Object id);

    Mono<T> updateReactive(T entity);

    Mono<Tuple2<Optional<Transaction>, Boolean>> deleteReactive(T obj);

    Mono<Boolean> deleteByIdReactive(Object id);

    Mono<Boolean> handDeleteByIdReactive(Object id);

    <S extends QueryBean<T, S>> Mono<Tuple2<Optional<Transaction>, Boolean>> handDeleteReactive(QueryBean<T, S> bean);

    Mono<T> saveReactive(T entity);

    <S extends QueryBean<T, S>> Mono<T> getOneReactive(QueryBean<T, S> bean);

    Mono<T> getByIdReactive(Object id);

    Mono<T> getByIdUseCacheReactive(Object id);

    Mono<List<T>> findAllReactive();

    <S extends QueryBean<T, S>> Mono<Optional<T>> findOneOptionalReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> Mono<T> findOneReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> Mono<T> findOneUseCacheReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> Mono<List<T>> findAllReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> Mono<List<T>> findAllUseCacheReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> Mono<Boolean> deleteReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> Page<T> page(QueryBean<T, S> bean, Page page);

    <S extends QueryBean<T, S>> Page<T> pageUseCache(QueryBean<T, S> bean, Page page);

    <S extends QueryBean<T, S>, R> Page<R> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T, S>, R> Page<R> pageUseCache(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T, S>, R> Page<R> page(QueryBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T, S>> int count(QueryBean<T, S> bean);


    <S extends QueryBean<T, S>> int countUseCache(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> boolean exists(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>, R> Page<R> page(QueryBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2);

    <S extends QueryBean<T, S>, R> Page<R> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2);

    <S extends QueryBean<T, S>> Mono<Page<T>> pageReactive(QueryBean<T, S> bean, Page page);

    <S extends QueryBean<T, S>> Mono<Page<T>> pageUseCacheReactive(QueryBean<T, S> bean, Page page);

    <S extends QueryBean<T, S>, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T, S>, R> Mono<? extends Page<? extends R>> pageUseCacheReactive(QueryBean<T, S> bean,
        Page page,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T, S>, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Consumer<Collection<T>> function, Function<? super T, ? extends R> mapper);

    <S extends QueryBean<T, S>> Mono<Integer> countReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>> Mono<Integer> countUseCacheReactive(QueryBean<T, S> bean);

    <S extends  QueryBean<T,S>> Mono<Boolean> existsReactive(QueryBean<T, S> bean);

    <S extends QueryBean<T, S>, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Consumer<Collection<T>> function, Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2);

    <S extends QueryBean<T, S>, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2);
}
