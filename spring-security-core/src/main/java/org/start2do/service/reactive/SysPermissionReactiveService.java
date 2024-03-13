package org.start2do.service.reactive;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.security.SysPermission;

@Service
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysPermissionReactiveService extends AbsReactiveService<SysPermission, Integer> {

}
