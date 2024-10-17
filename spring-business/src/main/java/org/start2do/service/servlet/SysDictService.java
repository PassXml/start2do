package org.start2do.service.servlet;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.query.QSysDictItem;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysDictService extends AbsService<SysDict> {

    private final SysDictItemService sysDictItemService;

    @Transactional(rollbackFor = Exception.class)
    public void remove(String id) {
        sysDictItemService.delete(new QSysDictItem().dictId.eq(id));
        deleteById(id);
    }
}
