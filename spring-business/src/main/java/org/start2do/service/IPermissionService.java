package org.start2do.service;

import java.util.List;
import org.start2do.dto.permission.PermissionAssignRequest;

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
}
