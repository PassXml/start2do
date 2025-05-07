package org.start2do.filter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.start2do.Start2doSecurityConfig;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.query.QSysPermission;
import org.start2do.entity.security.query.QSysUserRole;

@RequiredArgsConstructor
public abstract class AbsPermission {

    private final Start2doSecurityConfig config;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 提取用户权限
    public Set<String> extractPermissions(SysUser user) {
        // 从用户关联的权限表中提取权限URL
        List<SysPermission> list = new QSysPermission().select(QSysPermission.alias().url).roles.id.in(
            new QSysUserRole().select(QSysUserRole.alias().roleId).userId.eq(user.getId()).query()).users.id.in(
            user.getId()).findList();
        return list.stream().map(SysPermission::getUrl).collect(Collectors.toSet());
    }

    // 检查权限逻辑
    public boolean checkPermission(String requestPath, Set<String> permissions) {
        // 如果权限列表为空，根据默认配置决定是否放行
        boolean b = config.getPermission() == null || config.getPermission().isIgnoreDefaultUrlAuth();
        if (permissions.isEmpty()) {
            return b;
        }
        // 使用Spring的AntPathMatcher检查请求路径是否匹配用户权限列表中的路径
        return permissions.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath)) || b;
    }
}
