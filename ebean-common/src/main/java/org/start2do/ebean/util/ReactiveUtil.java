package org.start2do.ebean.util;

import io.ebean.DB;
import io.ebean.Transaction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.PermissionException;
import org.start2do.ebean.service.IReactiveService;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Slf4j
@UtilityClass
public class ReactiveUtil {

    /**
     * 租户或者用户id
     */
    public static final ThreadLocal TokenTreadLocal = new InheritableThreadLocal();


    public static Mono<Boolean> transaction(Consumer<Transaction> supplier) {
        return Mono.deferContextual(
            contextView -> Mono.zip(Mono.just(contextView.getOrEmpty(IReactiveService.TokenKey)),
                Mono.just(contextView.<Transaction>getOrEmpty(IReactiveService.TransactionKey))).map(o -> {
                Transaction transaction = o.getT2().orElseGet(DB::beginTransaction);
                o.getT1().ifPresent(TokenTreadLocal::set);
                return Mono.fromCallable(() -> {
                    supplier.accept(transaction);
                    return true;
                }).doFinally(signalType -> {
                    transaction.close();
                    TokenTreadLocal.remove();
                }).doOnError(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    transaction.rollback(throwable);
                }).doOnSuccess(t -> transaction.commit());
            })).flatMap(tMono -> tMono);
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

    public static void enableAutomaticContextPropagation(Runnable runnable) {
        Hooks.enableAutomaticContextPropagation();
        /**
         * implementation 'io.micrometer:context-propagation:1.1.1'
         *  ContextRegistry.getInstance()
         *                 .registerThreadLocalAccessor(IReactiveService.TokenKey, ReactiveUtil.TokenTreadLocal);
         */
        runnable.run();
    }


}
