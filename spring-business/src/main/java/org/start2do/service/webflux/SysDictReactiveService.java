package org.start2do.service.webflux;

import io.ebean.DB;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsMixService;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.query.QSysDictItem;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "dict", havingValue = "true", matchIfMissing = true)
public class SysDictReactiveService extends AbsMixService<SysDict, Integer> {

    private final SysDictItemReactiveService sysDictItemService;

    public Mono<Boolean> remove(String id) {
        return transactionOf(
            sysDictItemService.deleteReactive(new QSysDictItem().dictId.eq(id)).then(deleteByIdReactive(id)),
            DB.beginTransaction());
    }
}
