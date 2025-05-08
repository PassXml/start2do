package org.start2do.service.webflux;

import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsMixService;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.ebean.service.IMixService;
import org.start2do.entity.security.SysPositionEntity;

@Service
public class SysPositionReactiveService extends AbsMixService<SysPositionEntity, String> implements
    IMixService<SysPositionEntity> {

}
