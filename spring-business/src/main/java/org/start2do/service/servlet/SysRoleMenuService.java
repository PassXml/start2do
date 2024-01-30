package org.start2do.service.servlet;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysRoleMenu;

@RequiredArgsConstructor
@Service
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysRoleMenuService extends AbsService<SysRoleMenu> {

}
