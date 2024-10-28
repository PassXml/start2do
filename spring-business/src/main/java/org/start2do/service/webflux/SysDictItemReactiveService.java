package org.start2do.service.webflux;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsMixService;
import org.start2do.entity.business.SysDictItem;

@Service
@ConditionalOnWebApplication(type = Type.REACTIVE)

public class SysDictItemReactiveService extends AbsMixService<SysDictItem, Integer> {

}
