package org.start2do.ebean.service;

import static org.start2do.ebean.service.IReactiveService.TokenKey;
import static org.start2do.ebean.service.IReactiveService.TransactionKey;

import io.ebean.DB;
import io.ebean.Model;
import io.ebean.PagedList;
import io.ebean.Transaction;
import io.ebean.typequery.QueryBean;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.Page;
import org.start2do.ebean.EPage;
import org.start2do.ebean.service.AbsService.Runner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Slf4j
public abstract class AbsMixService<T extends Model,TokenType> implements IMixService<T> {

    protected final Class<T> aclass = getTClass();

    protected Class getTClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) actualTypeArguments[0];
    }

    @Override
    public T findOneById(Object id) {
        return DB.find(aclass).setId(id).findOne();
    }

    @Override
    public T findOneByIdUseCache(Object id) {
        return DB.find(aclass, id);
    }

    @Override
    public void update(T entity) {
        DB.update(entity);
    }

    @Override
    public void update(T entity, Transaction transaction) {
        entity.update(transaction);
    }


    @Override
    public boolean delete(T obj) {
        return obj.delete();
    }

    @Override
    public boolean delete(T obj, Transaction transaction) {
        return obj.delete(transaction);
    }

    @Override
    public int deleteById(Object id) {
        return DB.delete(aclass, id);
    }

    @Override
    public int handDeleteById(Object id) {
        return DB.deletePermanent(aclass, id);
    }

    @Override
    public <S extends QueryBean> boolean handDelete(QueryBean<T, S> bean) {
        return DB.deletePermanent(bean.findOne());
    }

    @Override
    public void save(T entity) {
        entity.save();
    }

    @Override
    public <S extends QueryBean> T getOne(QueryBean<T, S> bean) {
        return bean.findOneOrEmpty().orElseThrow(DataNotFoundException::new);
    }


    @Override
    public T getById(Object id) {
        return Optional.ofNullable(DB.find(aclass).setId(id).findOne()).orElseThrow(DataNotFoundException::new);
    }

    @Override
    public T getByIdUseCache(Object id) {
        return Optional.ofNullable(DB.find(aclass, id)).orElseThrow(DataNotFoundException::new);
    }

    @Override
    public List<T> findAll() {
        return DB.find(aclass).findList();
    }

    @Override
    public <S extends QueryBean> T findOne(QueryBean<T, S> bean) {
        return bean.findOne();
    }

    @Override
    public <S extends QueryBean> T findOneUseCache(QueryBean<T, S> bean) {
        bean.setUseQueryCache(true);
        return bean.findOne();
    }

    @Override
    public <S extends QueryBean> List<T> findAll(QueryBean<T, S> bean) {
        return bean.findList();
    }

    @Override
    public <S extends QueryBean> List<T> findAllUseCache(QueryBean<T, S> bean) {
        return bean.findList();
    }

    @Override
    public <S extends QueryBean> void delete(QueryBean<T, S> bean) {
        bean.delete();
    }

    @Override
    public <S extends QueryBean> Page<T> page(QueryBean<T, S> bean, Page page) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<T>(bean.findPagedList());
    }

    @Override
    public <S extends QueryBean> Page<T> pageUseCache(QueryBean<T, S> bean, Page page) {
        bean.setUseQueryCache(true);
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<T>(bean.findPagedList());
    }


    @Override
    public <S extends QueryBean, R> Page<R> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<R>(bean.findPagedList(), mapper);
    }

    @Override
    public <S extends QueryBean, R> Page<R> pageUseCache(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        bean.setUseQueryCache(true);
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<R>(bean.findPagedList(), mapper);
    }

    @Override
    public <S extends QueryBean, R> Page<R> page(QueryBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        PagedList<T> list = bean.findPagedList();
        if (function != null) {
            function.accept(list.getList());
        }
        return new EPage<>(list, mapper);
    }


    @Override
    public <S extends QueryBean> int count(QueryBean<T, S> bean) {
        return bean.findCount();
    }

    @Override
    public <S extends QueryBean> int countUseCache(QueryBean<T, S> bean) {
        return bean.findCount();
    }


    @Override
    public <S> boolean exists(QueryBean<T, S> bean) {
        return bean.exists();
    }

    @Override
    public <S extends QueryBean, R> Page<R> page(QueryBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        PagedList<T> list = bean.findPagedList();
        if (function != null) {
            function.accept(list.getList());
        }
        EPage<R> ePage = new EPage<>(list, mapper);
        if (function2 != null) {
            function2.accept(ePage.getRecords());
        }
        return ePage;
    }

    @Override
    public <S extends QueryBean, R> Page<R> page(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        PagedList<T> list = bean.findPagedList();
        EPage<R> ePage = new EPage<>(list, mapper);
        if (function2 != null) {
            function2.run(list.getList(), ePage.getRecords());
        }
        return ePage;
    }

    public void save(T entity, Transaction transaction) {
        entity.save(transaction);
    }



    public <TT> Mono<TT> transactionOf(Mono<TT> mono, Transaction transaction) {
        return mono.contextWrite(context -> {
//            if (context.hasKey(TransactionKey)) {
//                return context;
//            }
            return context.put(TransactionKey, transaction);
        }).doOnError(throwable -> {
            log.error(throwable.getMessage(), throwable);
            transaction.rollback(throwable);
        }).doFinally(signalType -> {
            transaction.commit();
            transaction.close();
        });
    }

    public <TT> Flux<TT> transactionOf(Flux<TT> flux, Transaction transaction) {
        return flux.contextWrite(context -> {
            if (context.hasKey(TransactionKey)) {
                return context;
            }
            return context.put(TransactionKey, transaction);
        }).doOnError(throwable -> {
            log.error(throwable.getMessage(), throwable);
            transaction.rollback(throwable);
        }).doFinally(signalType -> transaction.commit());
    }


    @Override
    public Mono<T> findOneByIdReactive(Object id) {
        return Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey)))
            .zipWith(Mono.just(id)).handle((objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    sink.next(DB.find(aclass).setId(id).findOne());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public Mono<T> findOneByIdUseCacheReactive(Object id) {
        return Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey)))
            .zipWith(Mono.just(id))
            .handle((BiConsumer<? super Tuple2<Optional<TokenType>, Object>, SynchronousSink<T>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    sink.next(DB.find(aclass, id));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            }).cache(Duration.ofSeconds(10));
    }

    @Override
    public Mono<T> updateReactive(T entity) {
        return Mono.zip(Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
                Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))), Mono.just(entity))
            .handle((objects, sink) -> {
//                objects.getT2().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    Optional<Transaction> optional = objects.getT1();
                    if (optional.isPresent()) {
                        entity.update(optional.get());
                    } else {
                        entity.update();
                    }
                    sink.next(entity);
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public Mono<Tuple2<Optional<Transaction>, Boolean>> deleteReactive(T obj) {
        return Mono.zip(Mono.just(obj).cast(aclass),
                Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
                Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))))
            .handle((objects, sink) -> {
//                objects.getT2().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                Optional<Transaction> optional = objects.getT3();
                try {
                    Boolean aBoolean = optional.map(transaction -> objects.getT1().delete(transaction))
                        .orElseGet(() -> objects.getT1().delete());
                    sink.next(Tuples.of(optional, aBoolean));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public Mono<Boolean> deleteByIdReactive(Object id) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
            Mono.just(id)).handle((objects, sink) -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                Object _id = objects.getT3();
                Boolean result = objects.getT2()
                    .map(transaction -> DB.getDefault().delete(aclass, _id, transaction) > 0)
                    .orElseGet(() -> DB.getDefault().delete(aclass, _id) > 0);
                ;
                if (result) {
                    sink.next(result);
                } else {
                    sink.error(new BusinessException("删除失败"));
                }
            } catch (Exception e) {
                sink.error(e);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public Mono<Boolean> handDeleteByIdReactive(Object id) {
        return Mono.zip(Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
                Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))), Mono.just(id))
            .handle((objects, sink) -> {
//                objects.getT2().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    Optional<Transaction> optional = objects.getT1();
                    Object _id = objects.getT3();
                    Boolean result;
                    result = optional.map(transaction -> DB.getDefault().deletePermanent(aclass, _id, transaction) > 0)
                        .orElseGet(() -> DB.getDefault().deletePermanent(aclass, _id) > 0);
                    if (result) {
                        sink.next(result);
                    } else {
                        sink.error(new BusinessException("删除失败"));
                    }
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends QueryBean> Mono<Tuple2<Optional<Transaction>, Boolean>> handDeleteReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
            Mono.just(bean.findOne())).handle((objects, sink) -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                T t3 = objects.getT3();
                Optional<Transaction> optional = objects.getT2();
                boolean deleted;
                deleted = optional.map(transaction -> objects.getT3().deletePermanent(transaction))
                    .orElseGet(() -> objects.getT3().deletePermanent());
                if (!deleted) {
                    sink.error(new BusinessException("删除失败"));
                } else {
                    sink.next(Tuples.of(objects.getT2(), deleted));
                }
            } catch (Exception e) {
                sink.error(e);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public Mono<T> saveReactive(T entity) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
            Mono.just(entity)).handle((objects, sink) -> {
            try {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                Optional<Transaction> optional = objects.getT2();
                if (optional.isPresent()) {
                    entity.save(optional.get());
                } else {
                    entity.save();
                }
                sink.next(entity);
            } catch (Exception e) {
                sink.error(e);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends QueryBean> Mono<T> getOneReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).mapNotNull(objects -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            return Optional.ofNullable(objects.getT2().findOne()).orElseThrow(DataNotFoundException::new);
        });
//            .doFinally(signalType -> ReactiveUtil.TokenTreadLocal.remove());
    }


    @Override
    public Mono<T> getByIdReactive(Object id) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(id)).handle((objects, sink) -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                sink.next(DB.find(aclass).setId(id).findOne());
            } catch (Exception e) {
                sink.error(e);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public Mono<T> getByIdUseCacheReactive(Object id) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
                Mono.just(id))
            .handle((BiConsumer<Tuple2<Optional<TokenType>, Object>, SynchronousSink<T>>) (objects, sink) -> {
                try {
//                    objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                    DB.find(aclass, id);
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            }).cache(Duration.ofSeconds(10));
    }

    @Override
    public Mono<List<T>> findAllReactive() {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(aclass)).handle((objects, sink) -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                sink.next(DB.find(aclass).findList());
            } catch (Exception exception) {
                sink.error(exception);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends QueryBean> Mono<Optional<T>> findOneOptionalReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle((objects, sink) -> {
            try {
                T one = objects.getT2().findOne();
                if (one == null) {
                    sink.next(Optional.empty());
                    return;
                }
                sink.next(Optional.of(one));
            } catch (Exception exception) {
                sink.error(exception);
                log.error(exception.getMessage(), exception);
            }
        });
    }

    @Override
    public <S extends QueryBean> Mono<T> findOneReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle((objects, sink) -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                T one = objects.getT2().findOne();
                if (one == null) {
                    return;
                }
                sink.next(one);
            } catch (Exception exception) {
                sink.error(exception);
                log.error(exception.getMessage(), exception);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends QueryBean> Mono<T> findOneUseCacheReactive(QueryBean<T, S> bean) {
        return findOneReactive(bean).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends QueryBean> Mono<List<T>> findAllReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle(
            (BiConsumer<? super Tuple2<Optional<TokenType>, QueryBean<T, S>>, SynchronousSink<List<T>>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    sink.next(objects.getT2().findList());
                } catch (Exception exception) {
                    sink.error(exception);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends QueryBean> Mono<List<T>> findAllUseCacheReactive(QueryBean<T, S> bean) {
        return findAllReactive(bean).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends QueryBean> Mono<Boolean> deleteReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
            Mono.just(bean)).<Boolean>handle((objects, sink) -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                QueryBean<T, S> rootBean = objects.getT3();
                objects.getT2().ifPresent(rootBean::usingTransaction);
                int delete = rootBean.delete();
                sink.next(true);
            } catch (Exception e) {
                sink.error(e);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends QueryBean> Mono<Page<T>> pageReactive(QueryBean<T, S> bean, Page page) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, QueryBean<T, S>, Page>, SynchronousSink<PagedList<T>>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    QueryBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(rootBean.findPagedList());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            }).map(EPage::new);
    }

    @Override
    public <S extends QueryBean> Mono<Page<T>> pageUseCacheReactive(QueryBean<T, S> bean, Page page) {
        return pageReactive(bean, page).cache(Duration.ofSeconds(10));
    }


    @Override
    public <S extends QueryBean, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, QueryBean<T, S>, Page>, SynchronousSink<Tuple2<PagedList<T>, Optional<TokenType>>>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    QueryBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(Tuples.of(rootBean.findPagedList(), objects.getT1()));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            }).zipWith(Mono.just(mapper)).map(objects -> {
//            objects.getT1().getT2().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                return new EPage<>(objects.getT1().getT1(), objects.getT2());
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends QueryBean, R> Mono<? extends Page<? extends R>> pageUseCacheReactive(QueryBean<T, S> bean,
        Page page,
        Function<? super T, ? extends R> mapper) {
        return pageReactive(bean, page, mapper).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends QueryBean, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Consumer<Collection<T>> function, Function<? super T, ? extends R> mapper) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, QueryBean<T, S>, Page>, SynchronousSink<Tuple2<PagedList<T>, Optional<TokenType>>>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    QueryBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(Tuples.of(rootBean.findPagedList(), objects.getT1()));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            }).zipWith(Mono.zip(Mono.just(function), Mono.just(mapper))).map(objects -> {
//            objects.getT1().getT2().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                Consumer<Collection<T>> consumer = objects.getT2().getT1();
                Function<? super T, ? extends R> t2 = objects.getT2().getT2();
                PagedList<T> list = objects.getT1().getT1();
                consumer.accept(list.getList());
                return new EPage<>(list, t2);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }


    @Override
    public <S extends QueryBean> Mono<Integer> countReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle(
            (BiConsumer<? super Tuple2<Optional<TokenType>, QueryBean<T, S>>, SynchronousSink<Integer>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    sink.next(bean.findCount());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends QueryBean> Mono<Integer> countUseCacheReactive(QueryBean<T, S> bean) {
        return countReactive(bean).cache(Duration.ofSeconds(10));
    }


    @Override
    public <S> Mono<Boolean> existsReactive(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle(
            (BiConsumer<? super Tuple2<Optional<TokenType>, QueryBean<T, S>>, SynchronousSink<Boolean>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    sink.next(bean.exists());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends QueryBean, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Consumer<Collection<T>> function, Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, QueryBean<T, S>, Page>, SynchronousSink<PagedList<T>>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    QueryBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(rootBean.findPagedList());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            }).zipWith(Mono.zip(Mono.just(function), Mono.just(mapper))).map(objects -> {
            PagedList<T> list = objects.getT1();
            objects.getT2().getT1().accept(list.getList());
            return new EPage<R>(list, objects.getT2().getT2());
        }).zipWith(Mono.just(function2)).map(objects -> {
            objects.getT2().accept(objects.getT1().getRecords());
            return objects.getT1();
        });
    }

    @Override
    public <S extends QueryBean, R> Mono<Page<R>> pageReactive(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, QueryBean<T, S>, Page>, SynchronousSink<Tuple2<PagedList<T>, Optional<TokenType>>>>) (objects, sink) -> {
//                objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
                try {
                    QueryBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(Tuples.of(rootBean.findPagedList(), objects.getT1()));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
//                    ReactiveUtil.TokenTreadLocal.remove();
                }
            }).zipWith(Mono.just(mapper)).map(objects -> {
//            objects.getT1().getT2().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                PagedList<T> list = objects.getT1().getT1();
                return Tuples.of(new EPage<R>(list, objects.getT2()), list, objects.getT1().getT2());
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        }).zipWith(Mono.just(function2)).map(objects -> {
//            objects.getT1().getT3().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            try {
                Tuple2<EPage<R>, PagedList<T>> tuple2 = objects.getT1();
                objects.getT2().run(tuple2.getT2().getList(), tuple2.getT1().getRecords());
                return tuple2.getT1();
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

}
