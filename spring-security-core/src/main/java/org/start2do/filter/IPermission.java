package org.start2do.filter;

import java.util.Collection;
import org.start2do.dto.UserCredentials;
import org.start2do.entity.security.SysPermission;

public interface IPermission {

    // 提取用户权限
    public Collection<SysPermission> extractPermissions(UserCredentials user);

    // 检查权限逻辑
    public boolean checkPermission(String requestPath, UserCredentials user, Collection<SysPermission> permissions);
}
