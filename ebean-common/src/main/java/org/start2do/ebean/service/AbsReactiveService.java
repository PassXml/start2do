package org.start2do.ebean.service;

import io.ebean.DB;
import io.ebean.Model;
import io.ebean.PagedList;
import io.ebean.Transaction;
import io.ebean.typequery.TQRootBean;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.Page;
import org.start2do.ebean.EPage;
import org.start2do.ebean.service.AbsService.Runner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public abstract class AbsReactiveService<T extends Model, TokenType> implements IReactiveService<T> {

    private final ThreadLocal<TokenType> TokenTreadLocal = new ThreadLocal<>();
    protected final Class<TokenType> TokenTypeClass = getTokenClass();
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

    public <TT> Mono<TT> transactionOf(Mono<TT> mono, Transaction transaction) {
        return mono.contextWrite(Context.of(TransactionKey, transaction)).doOnError(transaction::rollback)
            .doFinally(signalType -> transaction.commit());
    }

    public <TT> Flux<TT> transactionOf(Flux<TT> flux, Transaction transaction) {
        return flux.contextWrite(Context.of(TransactionKey, transaction)).doOnError(transaction::rollback)
            .doFinally(signalType -> transaction.commit());
    }


    @Override
    public Mono<T> findOneById(Object id) {
        return Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey)))
            .zipWith(Mono.just(id)).handle((objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    sink.next(DB.find(aclass).setId(id).findOne());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public Mono<T> findOneByIdUseCache(Object id) {
        return Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey)))
            .zipWith(Mono.just(id))
            .handle((BiConsumer<? super Tuple2<Optional<TokenType>, Object>, SynchronousSink<T>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    sink.next(DB.find(aclass, id));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            }).cache(Duration.ofSeconds(10));
    }

    @Override
    public Mono<Boolean> update(T entity) {
        return Mono.zip(Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
                Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))), Mono.just(entity))
            .handle((objects, sink) -> {
                objects.getT2().ifPresent(TokenTreadLocal::set);
                try {
                    Optional<Transaction> optional = objects.getT1();
                    if (optional.isPresent()) {
                        entity.update(optional.get());
                    } else {
                        entity.update();
                    }
                    sink.next(true);
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public Mono<Tuple2<Optional<Transaction>, Boolean>> delete(T obj) {
        return Mono.zip(Mono.just(obj).cast(aclass),
                Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
                Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))))
            .handle((objects, sink) -> {
                objects.getT2().ifPresent(TokenTreadLocal::set);
                Optional<Transaction> optional = objects.getT3();
                try {
                    Boolean aBoolean = optional.map(transaction -> objects.getT1().delete(transaction))
                        .orElseGet(() -> objects.getT1().delete());
                    sink.next(Tuples.of(optional, aBoolean));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public Mono<Integer> deleteById(Object id) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(id)).handle((objects, sink) -> {
            objects.getT1().ifPresent(TokenTreadLocal::set);
            try {
                sink.next(DB.delete(aclass, objects.getT2()));
            } catch (Exception e) {
                sink.error(e);
            } finally {
                TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public Mono<Integer> handDeleteById(Object id) {
        return Mono.zip(Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
                Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))), Mono.just(id))
            .handle((objects, sink) -> {
                objects.getT2().ifPresent(TokenTreadLocal::set);
                try {
                    sink.next(DB.deletePermanent(aclass, objects.getT2()));
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends TQRootBean> Mono<Tuple2<Optional<Transaction>, Boolean>> handDelete(TQRootBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
            Mono.just(bean.findOne())).handle((objects, sink) -> {
            objects.getT1().ifPresent(TokenTreadLocal::set);
            try {
                sink.next(Tuples.of(objects.getT2(), DB.deletePermanent(objects.getT3())));
            } catch (Exception e) {
                sink.error(e);
            } finally {
                TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public Mono<Boolean> save(T entity) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.<Optional<Transaction>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TransactionKey))),
            Mono.just(entity)).handle((objects, sink) -> {
            try {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                Optional<Transaction> optional = objects.getT2();
                if (optional.isPresent()) {
                    entity.save(optional.get());
                } else {
                    entity.save();
                }
                sink.next(true);
            } catch (Exception e) {
                sink.error(e);
            } finally {
                TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends TQRootBean> Mono<T> getOne(TQRootBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).mapNotNull(objects -> {
            objects.getT1().ifPresent(TokenTreadLocal::set);
            return Optional.ofNullable(objects.getT2().findOne()).orElseThrow(DataNotFoundException::new);
        }).doFinally(signalType -> TokenTreadLocal.remove());
    }


    @Override
    public Mono<T> getById(Object id) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(id)).handle((objects, sink) -> {
            objects.getT1().ifPresent(TokenTreadLocal::set);
            try {
                sink.next(DB.find(aclass).setId(id).findOne());
            } catch (Exception e) {
                sink.error(e);
            } finally {
                TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public Mono<T> getByIdUseCache(Object id) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
                Mono.just(id))
            .handle((BiConsumer<Tuple2<Optional<TokenType>, Object>, SynchronousSink<T>>) (objects, sink) -> {
                try {
                    objects.getT1().ifPresent(TokenTreadLocal::set);
                    DB.find(aclass, id);
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            }).cache(Duration.ofSeconds(10));
    }

    @Override
    public Mono<List<T>> findAll() {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(aclass)).handle((objects, sink) -> {
            objects.getT1().ifPresent(TokenTreadLocal::set);
            try {
                sink.next(DB.find(aclass).findList());
            } catch (Exception exception) {
                sink.error(exception);
            } finally {
                TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends TQRootBean> Mono<T> findOne(TQRootBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle((objects, sink) -> {
            objects.getT1().ifPresent(TokenTreadLocal::set);
            try {
                sink.next(objects.getT2().findOne());
            } catch (Exception exception) {
                sink.error(exception);
            } finally {
                TokenTreadLocal.remove();
            }
        });
    }

    @Override
    public <S extends TQRootBean> Mono<T> findOneUseCache(TQRootBean<T, S> bean) {
        return findOne(bean).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends TQRootBean> Mono<List<T>> findAll(TQRootBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle(
            (BiConsumer<? super Tuple2<Optional<TokenType>, TQRootBean<T, S>>, SynchronousSink<List<T>>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    sink.next(objects.getT2().findList());
                } catch (Exception exception) {
                    sink.error(exception);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends TQRootBean> Mono<List<T>> findAllUseCache(TQRootBean<T, S> bean) {
        return findAll(bean).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends TQRootBean> Mono<Boolean> delete(TQRootBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle(
            (BiConsumer<? super Tuple2<Optional<TokenType>, TQRootBean<T, S>>, SynchronousSink<Boolean>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    sink.next(objects.getT2().delete() > 0);
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends TQRootBean> Mono<Page<T>> page(TQRootBean<T, S> bean, Page page) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, TQRootBean<T, S>, Page>, SynchronousSink<PagedList<T>>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    TQRootBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(rootBean.findPagedList());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            }).map(EPage::new);
    }

    @Override
    public <S extends TQRootBean> Mono<Page<T>> pageUseCache(TQRootBean<T, S> bean, Page page) {
        return page(bean, page).cache(Duration.ofSeconds(10));
    }


    @Override
    public <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, TQRootBean<T, S>, Page>, SynchronousSink<PagedList<T>>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    TQRootBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(rootBean.findPagedList());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            }).zipWith(Mono.just(mapper)).map(objects -> new EPage<>(objects.getT1(), objects.getT2()));
    }

    @Override
    public <S extends TQRootBean, R> Mono<? extends Page<? extends R>> pageUseCache(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        return page(bean, page, mapper).cache(Duration.ofSeconds(10));
    }

    @Override
    public <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Consumer<Collection<T>> function, Function<? super T, ? extends R> mapper) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, TQRootBean<T, S>, Page>, SynchronousSink<PagedList<T>>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    TQRootBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(rootBean.findPagedList());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            }).zipWith(Mono.zip(Mono.just(function), Mono.just(mapper))).map(objects -> {
            Consumer<Collection<T>> consumer = objects.getT2().getT1();
            Function<? super T, ? extends R> t2 = objects.getT2().getT2();
            PagedList<T> list = objects.getT1();
            consumer.accept(list.getList());
            return new EPage<>(list, t2);
        });
    }


    @Override
    public <S extends TQRootBean> Mono<Integer> count(TQRootBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle(
            (BiConsumer<? super Tuple2<Optional<TokenType>, TQRootBean<T, S>>, SynchronousSink<Integer>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    sink.next(bean.findCount());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends TQRootBean> Mono<Integer> countUseCache(TQRootBean<T, S> bean) {
        return count(bean).cache(Duration.ofSeconds(10));
    }


    @Override
    public <S> Mono<Boolean> exists(TQRootBean<T, S> bean) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean)).handle(
            (BiConsumer<? super Tuple2<Optional<TokenType>, TQRootBean<T, S>>, SynchronousSink<Boolean>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    sink.next(bean.exists());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            });
    }

    @Override
    public <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Consumer<Collection<T>> function, Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, TQRootBean<T, S>, Page>, SynchronousSink<PagedList<T>>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    TQRootBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(rootBean.findPagedList());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
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
    public <S extends TQRootBean, R> Mono<Page<R>> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2) {
        return Mono.zip(Mono.<Optional<TokenType>>deferContextual(ctx -> Mono.just(ctx.getOrEmpty(TokenKey))),
            Mono.just(bean), Mono.just(page)).handle(
            (BiConsumer<? super Tuple3<Optional<TokenType>, TQRootBean<T, S>, Page>, SynchronousSink<PagedList<T>>>) (objects, sink) -> {
                objects.getT1().ifPresent(TokenTreadLocal::set);
                try {
                    TQRootBean<T, S> rootBean = objects.getT2();
                    rootBean.setMaxRows(objects.getT3().getSize()).setFirstRow(objects.getT3().getOffset());
                    sink.next(rootBean.findPagedList());
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    TokenTreadLocal.remove();
                }
            }).zipWith(Mono.just(mapper)).map(objects -> {
            PagedList<T> list = objects.getT1();
            return Tuples.of(new EPage<R>(list, objects.getT2()), list);
        }).zipWith(Mono.just(function2)).map(objects -> {
            Tuple2<EPage<R>, PagedList<T>> tuple2 = objects.getT1();
            objects.getT2().run(tuple2.getT2().getList(), tuple2.getT1().getRecords());
            return tuple2.getT1();
        });
    }

}
