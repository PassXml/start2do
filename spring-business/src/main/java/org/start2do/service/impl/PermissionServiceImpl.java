package org.start2do.service.impl;

import io.ebean.DB;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.start2do.dto.permission.PermissionAssignRequest;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.SysPermissionRoleRef;
import org.start2do.entity.security.SysPermissionRoleRefId;
import org.start2do.entity.security.SysPermissionUserRef;
import org.start2do.entity.security.SysPermissionUserRefId;
import org.start2do.service.IPermissionService;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    @Override
    @Transactional
    public boolean assignToUsers(String permissionId, List<String> userIds) {
        // 检查权限是否存在
        SysPermission permission = DB.find(SysPermission.class, permissionId);
        if (permission == null) {
            return false;
        }
        
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        
        // 先删除现有的关联
        DB.deleteAll(DB.find(SysPermissionUserRef.class)
            .where()
            .eq("permissionId", permissionId)
            .findList());
            
        List<SysPermissionUserRef> refs = new ArrayList<>();
        for (String userId : userIds) {
            if (StringUtils.hasText(userId)) {
                SysPermissionUserRefId id = new SysPermissionUserRefId(permissionId, userId);
                SysPermissionUserRef ref = new SysPermissionUserRef(id);
                ref.setPermissionId(permissionId);
                ref.setUserId(userId);
                refs.add(ref);
            }
        }
        
        if (!refs.isEmpty()) {
            DB.saveAll(refs);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean assignToRoles(String permissionId, List<String> roleIds) {
        // 检查权限是否存在
        SysPermission permission = DB.find(SysPermission.class, permissionId);
        if (permission == null) {
            return false;
        }
        
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        
        // 先删除现有的关联
        DB.deleteAll(DB.find(SysPermissionRoleRef.class)
            .where()
            .eq("permissionId", permissionId)
            .findList());
            
        List<SysPermissionRoleRef> refs = new ArrayList<>();
        for (String roleId : roleIds) {
            if (StringUtils.hasText(roleId)) {
                SysPermissionRoleRefId id = new SysPermissionRoleRefId(permissionId, roleId);
                SysPermissionRoleRef ref = new SysPermissionRoleRef(id);
                ref.setPermissionId(permissionId);
                ref.setRoleId(roleId);
                refs.add(ref);
            }
        }
        
        if (!refs.isEmpty()) {
            DB.saveAll(refs);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean assignPermission(PermissionAssignRequest request) {
        boolean userResult = true;
        boolean roleResult = true;
        
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            userResult = assignToUsers(request.getPermissionId(), request.getUserIds());
        }
        
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            roleResult = assignToRoles(request.getPermissionId(), request.getRoleIds());
        }
        
        return userResult && roleResult;
    }

    @Override
    @Transactional
    public boolean removeUserPermission(String permissionId, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        
        for (String userId : userIds) {
            if (StringUtils.hasText(userId)) {
                SysPermissionUserRefId id = new SysPermissionUserRefId(permissionId, userId);
                DB.delete(SysPermissionUserRef.class, id);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean removeRolePermission(String permissionId, List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        
        for (String roleId : roleIds) {
            if (StringUtils.hasText(roleId)) {
                SysPermissionRoleRefId id = new SysPermissionRoleRefId(permissionId, roleId);
                DB.delete(SysPermissionRoleRef.class, id);
            }
        }
        return true;
    }
}
