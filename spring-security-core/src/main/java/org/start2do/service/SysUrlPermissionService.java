package org.start2do.service;

import java.util.Collection;
import java.util.List;
import org.start2do.ebean.service.IService;
import org.start2do.entity.security.SysUrlPermission;

public interface SysUrlPermissionService extends IService<SysUrlPermission> {


    List<SysUrlPermission> findAllByRolesOrUserId(Collection<String> roles, String userId);
}
