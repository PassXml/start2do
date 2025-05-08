package org.start2do.filter;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.start2do.config.PermissionConfig;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.UserRole;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.query.QSysPermission;
import org.start2do.entity.security.query.QSysUserRole;

@RequiredArgsConstructor
public abstract class AbsPermission {

    protected final PermissionConfig config;
    protected final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 提取用户权限
    public Collection<SysPermission> extractPermissions(UserCredentials user) {
        // 从用户关联的权限表中提取权限URL
        return new QSysPermission().select(QSysPermission.alias().url).roles.id.in(
            new QSysUserRole().select(QSysUserRole.alias().roleId).userId.eq(user.getId()).query()).users.id.in(
            user.getId()).findList();
    }

    // 检查权限逻辑
    public boolean checkPermission(String requestPath, UserCredentials user, Collection<SysPermission> permissions) {
        // 如果权限列表为空，根据默认配置决定是否放行
        if (!config.isEnable()) {
            return true;
        }
        if (config.getIgnoreDefaultRole().stream()
            .anyMatch(s -> user.getRoles().stream().map(UserRole::getRoleCode).anyMatch(s1 -> s1.equals(s)))) {
            return true;
        }
        // 使用Spring的AntPathMatcher检查请求路径是否匹配用户权限列表中的路径
        return permissions.stream()
            .anyMatch(pattern -> pattern.isPass() || pathMatcher.match(pattern.getUrl(), requestPath));
    }
}
