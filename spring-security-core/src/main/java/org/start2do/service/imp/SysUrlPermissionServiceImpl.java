package org.start2do.service.imp;

import java.util.Collection;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsService;
import org.start2do.ebean.util.EntityHook;
import org.start2do.entity.security.SysUrlPermission;
import org.start2do.entity.security.SysUrlPermission.SourceType;
import org.start2do.entity.security.query.QSysUrlPermission;
import org.start2do.service.SysUrlPermissionService;

@Service
public class SysUrlPermissionServiceImpl extends AbsService<SysUrlPermission> implements SysUrlPermissionService,
    EntityHook<SysUrlPermission> {


    @Override
    @Cacheable(key = "'SysUrlPermissionServiceImpl@findAllByRolesOrUserId'", value = "#userId+#String.join(\",\",#roles)")
    public List<SysUrlPermission> findAllByRolesOrUserId(Collection<String> roles, String userId) {
        return new QSysUrlPermission().setUseCache(true).setUseQueryCache(true).sourceId.in(roles).sourceType.eq(
            SourceType.Role).or().sourceId.eq(userId).sourceType.eq(SourceType.User).endOr().findList();
    }

    @Override
    public Class<SysUrlPermission> getKey() {
        return SysUrlPermission.class;
    }
}
