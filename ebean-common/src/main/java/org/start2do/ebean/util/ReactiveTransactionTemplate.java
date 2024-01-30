package org.start2do.ebean.util;

import java.util.concurrent.ThreadFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.start2do.util.ThreadUtil;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

@Primary
@Component
@RequiredArgsConstructor
public class ReactiveTransactionTemplate extends DefaultTransactionDefinition {


    private final PlatformTransactionManager transactionManager;
    private final ThreadFactory factory = new ThreadUtil.CustomThreadFactory("System-Transaction");

    public <T> Mono<T> execute(ReactiveTransactionCallback<T> action) {
        Scheduler scheduler = Schedulers.newSingle(factory);
        return Mono.just(true)
            .publishOn(scheduler)
            .map(b -> transactionManager.getTransaction(this))
            .zipWhen(status -> doAndError(action, status, scheduler))
            .publishOn(scheduler)
            .doOnNext(z -> transactionManager.commit(z.getT1()))
            .map(Tuple2::getT2)
            .doFinally(t -> scheduler.dispose());
    }

    private <T> Mono<T> doAndError(ReactiveTransactionCallback<T> action, TransactionStatus status,
        Scheduler scheduler) {
        return action.doInTransaction(status)
            .publishOn(scheduler)
            .doOnError(e -> {
                status.setRollbackOnly();
                transactionManager.rollback(status);
            });
    }

    public interface ReactiveTransactionCallback<T> {

        Mono<T> doInTransaction(TransactionStatus status);
    }

}
