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

public interface IReactiveService<T extends Model> {

    String TokenKey = "_TOKEN_KEY_";
    String TransactionKey = "_Transaction_";
    String UserNameKey = "_USERNAME_";


    Mono<T> findOneById(Object id);


    Mono<T> findOneByIdUseCache(Object id);

    Mono<T> update(T entity);

    Mono<Tuple2<Optional<Transaction>, Boolean>> delete(T obj);

    Mono<Boolean> deleteById(Object id);

    Mono<Boolean> handDeleteById(Object id);

    <S extends QueryBean> Mono<Tuple2<Optional<Transaction>, Boolean>> handDelete(QueryBean<T, S> bean);

    Mono<T> save(T entity);

    <S extends QueryBean> Mono<T> getOne(QueryBean<T, S> bean);

    Mono<T> getById(Object id);

    Mono<T> getByIdUseCache(Object id);

    Mono<List<T>> findAll();

    <S extends QueryBean> Mono<T> findOne(QueryBean<T, S> bean);

    <S extends QueryBean> Mono<T> findOneUseCache(QueryBean<T, S> bean);

    <S extends QueryBean> Mono<List<T>> findAll(QueryBean<T, S> bean);

    <S extends QueryBean> Mono<List<T>> findAllUseCache(QueryBean<T, S> bean);

    <S extends QueryBean> Mono<Boolean> delete(QueryBean<T, S> bean);

    <S extends QueryBean> Mono<Page<T>> page(QueryBean<T, S> bean, Page page);

    <S extends QueryBean> Mono<Page<T>> pageUseCache(QueryBean<T, S> bean, Page page);

    <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean, R> Mono<? extends Page<? extends R>> pageUseCache(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper);

    //    public <S extends QueryBean, R> Page<R> page(QueryBean<T, S> bean, Page page, Consumer<Collection<T>> function,
//        Function<? super T, ? extends R> mapper)
    <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
        Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper);

    <S extends QueryBean> Mono<Integer> count(QueryBean<T, S> bean);

    <S extends QueryBean> Mono<Integer> countUseCache(QueryBean<T, S> bean);

    <S> Mono<Boolean> exists(QueryBean<T, S> bean);

    <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
        Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2);

    <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2);
}
