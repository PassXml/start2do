package org.start2do.filter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.AntPathMatcher;
import org.start2do.config.PermissionConfig;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.UserRole;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.query.QSysPermission;
import org.start2do.entity.security.query.QSysPermissionRoleRef;
import org.start2do.entity.security.query.QSysPermissionUserRef;

@RequiredArgsConstructor
public abstract class AbsPermission {

  protected final PermissionConfig config;
  protected final AntPathMatcher pathMatcher = new AntPathMatcher();

  // 提取用户权限
  @Cacheable(key = "#user.id")
  public Collection<SysPermission> extractPermissions(UserCredentials user) {
    // 从用户关联的权限表中提取权限URL
    return new QSysPermission()
        .select(QSysPermission.alias().url, QSysPermission.alias().pass)
        .or()
        .id
        .in(
            new QSysPermissionUserRef()
                .select(QSysPermissionUserRef.alias().permissionId)
                .userId
                .eq(user.getId())
                .query())
        .id
        .in(
            new QSysPermissionRoleRef()
                .select(QSysPermissionRoleRef.alias().permissionId)
                .roleId
                .in(user.getRoles().stream().map(UserRole::getRoleId).distinct().toList())
                .query())
        .endOr()
        .findList();
  }

  // 提取用户权限
  @Cacheable
  public Collection<SysPermission> findAll() {
    // 从用户关联的权限表中提取权限URL
    return new QSysPermission()
        .select(QSysPermission.alias().url, QSysPermission.alias().pass)
        .findList();
  }

  // 检查权限逻辑
  public boolean checkPermission(
      String requestPath, UserCredentials user, Collection<SysPermission> userSpecificPermissions) {
    // 1. 如果权限系统未启用，则直接放行
    if (!config.isEnable()) {
      return true;
    }

    // 2. 检查用户是否拥有配置中指定的应忽略权限检查的角色 (例如超级管理员角色)
    //    优化了角色检查的可读性
    Set<String> userRoleCodes = user.getRoles().stream()
        .map(UserRole::getRoleCode)
        .collect(Collectors.toSet());
    if (config.getIgnoreDefaultRole().stream().anyMatch(userRoleCodes::contains)) {
      return true;
    }

    // 3. 检查用户特定的权限 (直接分配或通过角色继承)
    //    如果找到匹配的特定权限，则该权限的 isPass() 状态决定访问权限，并立即返回结果。
    for (SysPermission userPermission : userSpecificPermissions) {
      if (userPermission.getUrl() != null && pathMatcher.match(userPermission.getUrl(), requestPath)) {
        return true;
      }
    }

    // 4. 如果在用户特定权限中未找到匹配项，则检查所有系统定义的“全局”权限规则。
    //    这用于确定路径是否通常受保护以及其默认访问状态。
    Collection<SysPermission> allSystemPermissions = findAll(); // 此方法已使用 @Cacheable 缓存
    for (SysPermission systemPermission : allSystemPermissions) {
      if (systemPermission.getUrl() != null && pathMatcher.match(systemPermission.getUrl(), requestPath)) {
        // 如果路径与系统定义的权限匹配，则其 isPass() 标志决定访问权限，并立即返回结果。
        return systemPermission.isPass();
      }
    }

    // 5. 如果请求路径与任何用户特定权限或任何系统定义的权限都不匹配：
    //    默认拒绝访问。这是一种安全的默认设置，意味着只有明确配置允许的路径才可访问。
    return false;
  }
}
