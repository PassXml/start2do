package org.start2do.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysUserPermission;

@DependsOn({"Database"})
@Service
@RequiredArgsConstructor
public class SysLoginUserPermissionService extends AbsService<SysUserPermission> {

}
