package org.start2do.service;

import java.util.List;
import org.start2do.dto.Page;
import org.start2do.dto.permission.PermissionAssignRequest;
import org.start2do.dto.permission.PermissionDetailDto;
import org.start2do.dto.permission.PermissionPageRequest;
import org.start2do.entity.security.SysPermission;

/**
 * 权限管理服务接口
 */
public interface IPermissionService {

    /**
     * 关联用户与权限
     * @param permissionId 权限ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    boolean assignToUsers(String permissionId, List<String> userIds);
    
    /**
     * 关联角色与权限
     * @param permissionId 权限ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    boolean assignToRoles(String permissionId, List<String> roleIds);
    
    /**
     * 批量关联用户和角色
     * @param request 关联请求
     * @return 操作结果
     */
    boolean assignPermission(PermissionAssignRequest request);
    
    /**
     * 移除用户权限关联
     * @param permissionId 权限ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    boolean removeUserPermission(String permissionId, List<String> userIds);
    
    /**
     * 移除角色权限关联
     * @param permissionId 权限ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    boolean removeRolePermission(String permissionId, List<String> roleIds);
    
    /**
     * 分页查询权限
     * @param request 查询请求
     * @return 分页结果
     */
    Page<SysPermission> page(PermissionPageRequest request);
    
    /**
     * 获取权限详情
     * @param id 权限ID
     * @return 权限详情
     */
    PermissionDetailDto getDetail(String id);
}
