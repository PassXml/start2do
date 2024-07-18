package org.start2do.ebean.service;

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
import java.util.function.Supplier;
import org.slf4j.Logger;
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

public abstract class AbsReactiveService<T extends Model, TokenType> implements IReactiveService<T> {

    //    protected final Class<TokenType> TokenTypeClass = getTokenClass();
    protected final Class<T> aclass = getTClass();
    protected Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    protected Class getTClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) actualTypeArguments[0];

    }

    protected Class getTokenClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) actualTypeArguments[1];
    }

    public Mono<Boolean> checkBooleanResult(Mono<Boolean> mono, Supplier<Exception> supplier) {
        return mono.filter(tt -> tt).switchIfEmpty(Mono.error(supplier.get()));
    }

    public <TT> Mono<TT> transactionOf(Mono<TT> mono, Transaction transaction) {
        return mono.contextWrite(context -> {
//            if (context.hasKey(TransactionKey)) {
//                return context;
//            }
            return context.put(TransactionKey, transaction);
        }).doOnError(throwable -> {
            logger.error(throwable.getMessage(), throwable);
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
            logger.error(throwable.getMessage(), throwable);
            transaction.rollback(throwable);
        }).doFinally(signalType -> transaction.commit());
    }


    @Override
    public Mono<T> findOneById(Object id) {
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
    public Mono<T> findOneByIdUseCache(Object id) {
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
    public Mono<T> update(T entity) {
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
    public Mono<Tuple2<Optional<Transaction>, Boolean>> delete(T obj) {
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
    public Mono<Boolean> deleteById(Object id) {
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
    public Mono<Boolean> handDeleteById(Object id) {
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
    public <S extends QueryBean> Mono<Tuple2<Optional<Transaction>, Boolean>> handDelete(QueryBean<T, S> bean) {
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
    public Mono<T> save(T entity) {
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
    public <S extends QueryBean> Mono<T> getOne(QueryBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).mapNotNull(objects -> {
//            objects.getT1().ifPresent(ReactiveUtil.TokenTreadLocal::set);
            return Optional.ofNullable(objects.getT2().findOne()).orElseThrow(DataNotFoundException::new);
        });
//            .doFinally(signalType -> ReactiveUtil.TokenTreadLocal.remove());
    }


    @Override
    public Mono<T> getById(Object id) {
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
    public Mono<T> getByIdUseCache(Object id) {
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
    public Mono<List<T>> findAll() {
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
    public <S extends QueryBean> Mono<Optional<T>> findOneOptional(QueryBean<T, S> bean) {
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
                logger.error(exception.getMessage(), exception);
            }
        });
    }

    @Override
    public <S extends QueryBean> Mono<T> findOne(QueryBean<T, S> bean) {
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
                logger.error(exception.getMessage(), exception);
            } finally {
//                ReactiveUtil.TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends QueryBean> Mono<T> findOneUseCache(QueryBean<T, S> bean) {
        return findOne(bean).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends QueryBean> Mono<List<T>> findAll(QueryBean<T, S> bean) {
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
    public <S extends QueryBean> Mono<List<T>> findAllUseCache(QueryBean<T, S> bean) {
        return findAll(bean).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends QueryBean> Mono<Boolean> delete(QueryBean<T, S> bean) {
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
    public <S extends QueryBean> Mono<Page<T>> page(QueryBean<T, S> bean, Page page) {
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
    public <S extends QueryBean> Mono<Page<T>> pageUseCache(QueryBean<T, S> bean, Page page) {
        return page(bean, page).cache(Duration.ofSeconds(10));
    }


    @Override
    public <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
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
    public <S extends QueryBean, R> Mono<? extends Page<? extends R>> pageUseCache(QueryBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        return page(bean, page, mapper).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
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
    public <S extends QueryBean> Mono<Integer> count(QueryBean<T, S> bean) {
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
    public <S extends QueryBean> Mono<Integer> countUseCache(QueryBean<T, S> bean) {
        return count(bean).cache(Duration.ofSeconds(10));
    }


    @Override
    public <S> Mono<Boolean> exists(QueryBean<T, S> bean) {
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
    public <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
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
    public <S extends QueryBean, R> Mono<Page<R>> page(QueryBean<T, S> bean, Page page,
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
