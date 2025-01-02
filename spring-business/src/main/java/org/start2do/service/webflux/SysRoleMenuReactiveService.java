package org.start2do.service.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsMixService;
import org.start2do.entity.security.SysRoleMenu;

@RequiredArgsConstructor
@Service
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "role", havingValue = "true",matchIfMissing = true)
public class SysRoleMenuReactiveService extends AbsMixService<SysRoleMenu, Integer> {

}
