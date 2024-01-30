package org.start2do.service.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.security.SysRoleMenu;

@RequiredArgsConstructor
@Service
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysRoleMenuReactiveService extends AbsReactiveService<SysRoleMenu, Integer> {

}
