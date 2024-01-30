package org.start2do.ebean.util;

import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.PermissionException;
import org.start2do.ebean.service.IReactiveService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@UtilityClass
public class ReactiveUtil {

    public static final ThreadLocal TokenTreadLocal = new ThreadLocal<>();

    /**
     * 调度器，使用单一线程，并且在切换的时候设置线程变量，完成后移除
     */
    public static <T> Mono<T> single(Mono<T> mono) {
        //调度器，使用单一线程
        Scheduler scheduler = Schedulers.single();
        return Mono.deferContextual(
                contextView -> Mono.just(contextView.getOrEmpty(IReactiveService.TokenKey)))
            .doOnNext(o -> {
                o.ifPresent(TokenTreadLocal::set);
            }).publishOn(scheduler).flatMap(o -> mono).doOnError(throwable -> TokenTreadLocal.remove())
            .doFinally(signalType -> TokenTreadLocal.remove());
    }

    public static <T> Mono<T> injectTokenInfoSingle(Supplier<T> supplier) {
        //调度器，使用单一线程
        Scheduler scheduler = Schedulers.single();
        return Mono.deferContextual(contextView -> Mono.just(contextView.getOrEmpty(IReactiveService.TokenKey)))
            .doOnNext(o -> {
                o.ifPresent(TokenTreadLocal::set);
            }).publishOn(scheduler).map(o -> supplier.get()).doOnError(throwable -> TokenTreadLocal.remove())
            .doFinally(signalType -> TokenTreadLocal.remove());
    }

    public static <T> Mono<T> injectTokenInfo(Supplier<T> supplier) {
        return Mono.deferContextual(
            contextView -> Mono.just(contextView.getOrEmpty(IReactiveService.TokenKey)).map(o -> {
                o.ifPresent(TokenTreadLocal::set);
                try {
                    return supplier.get();
                } finally {
                    TokenTreadLocal.remove();
                }
            }));
    }

    public static <T> Mono<T> injectTokenInfo(Supplier<T> supplier, int i) {
        return Mono.<T>fromSupplier(() -> {
            TokenTreadLocal.set(i);
            try {
                return supplier.get();
            } finally {
                TokenTreadLocal.remove();
            }
        });
    }

    public static <R> Mono<R> log(Throwable throwable, Supplier<Exception> exception) {
        log.error(throwable.getMessage(), throwable);
        if ((throwable instanceof BusinessException || throwable instanceof DataNotFoundException
            || throwable instanceof PermissionException)) {
            return Mono.error(throwable);
        }
        return Mono.error(exception.get());
    }


}
