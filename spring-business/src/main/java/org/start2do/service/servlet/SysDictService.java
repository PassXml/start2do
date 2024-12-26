package org.start2do.service.servlet;

import io.ebean.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.query.QSysDictItem;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.service", name = "dict", havingValue = "true",matchIfMissing = true)
public class SysDictService extends AbsService<SysDict> {

    private final SysDictItemService sysDictItemService;

    @Transactional(rollbackFor = Exception.class)
    public void remove(String id) {
        sysDictItemService.delete(new QSysDictItem().dictId.eq(id));
        deleteById(id);
    }
}
