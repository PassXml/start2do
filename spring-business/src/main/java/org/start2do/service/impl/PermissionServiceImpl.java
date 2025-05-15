package org.start2do.service.impl;

import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.start2do.dto.Page;
import org.start2do.dto.permission.PermissionAssignRequest;
import org.start2do.dto.permission.PermissionDetailDto;
import org.start2do.dto.permission.PermissionPageRequest;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.SysPermissionRoleRef;
import org.start2do.entity.security.SysPermissionRoleRefId;
import org.start2do.entity.security.SysPermissionUserRef;
import org.start2do.entity.security.SysPermissionUserRefId;
import org.start2do.service.IPermissionService;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends AbsService<SysPermission> implements IPermissionService {

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

    @Override
    public Page<SysPermission> page(PermissionPageRequest request) {
        Query<SysPermission> query = DB.find(SysPermission.class);
        ExpressionList<SysPermission> where = query.where();
        
        // 添加条件过滤
        if (StringUtils.hasText(request.getUrl())) {
            where.ilike("url", "%" + request.getUrl() + "%");
        }
        
        if (request.getPass() != null) {
            where.eq("pass", request.getPass());
        }
        
        // 设置排序
        query.orderBy("id asc");
        
        // 执行查询
        PagedList<SysPermission> pagedList = query
            .setFirstRow(request.getOffset())
            .setMaxRows(request.getSize())
            .findPagedList();
            
        return new Page<>(
            pagedList.getTotalCount(),
            request.getSize(),
            request.getCurrent(),
            pagedList.getList()
        );
    }

    @Override
    public PermissionDetailDto getDetail(String id) {
        // 查询权限
        SysPermission permission = DB.find(SysPermission.class)
                .setId(id)
                .fetch("users")
                .fetch("roles")
                .findOne();
                
        if (permission == null) {
            return null;
        }
        
        // 构建详情DTO
        PermissionDetailDto detailDto = new PermissionDetailDto()
                .setId(permission.getId())
                .setUrl(permission.getUrl())
                .setPass(permission.isPass());
                
        // 转换用户列表
        if (permission.getUsers() != null) {
            List<PermissionDetailDto.UserDto> userDtos = permission.getUsers().stream()
                    .map(user -> new PermissionDetailDto.UserDto()
                            .setId(user.getId())
                            .setUsername(user.getUsername()))
                    .collect(Collectors.toList());
            detailDto.setUsers(userDtos);
        }
        
        // 转换角色列表
        if (permission.getRoles() != null) {
            List<PermissionDetailDto.RoleDto> roleDtos = permission.getRoles().stream()
                    .map(role -> new PermissionDetailDto.RoleDto()
                            .setId(role.getId())
                            .setRoleName(role.getRoleName()))
                    .collect(Collectors.toList());
            detailDto.setRoles(roleDtos);
        }
        
        return detailDto;
    }
}
