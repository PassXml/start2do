package org.start2do.service.webflux;

import io.ebean.DB;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.query.QSysDictItem;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysDictReactiveService extends AbsReactiveService<SysDict, Integer> {

    private final SysDictItemReactiveService sysDictItemService;

    public Mono<Boolean> remove(UUID id) {
        return transactionOf(sysDictItemService.delete(new QSysDictItem().dictId.eq(id)).then(deleteById(id)),
            DB.beginTransaction());
    }
}
