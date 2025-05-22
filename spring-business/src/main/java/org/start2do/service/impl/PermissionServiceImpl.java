package org.start2do.service.impl;

import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.start2do.constant.Constant;
import org.start2do.controller.AbsPermissionController;
import org.start2do.dto.Page;
import org.start2do.dto.permission.PermissionDetailResp;
import org.start2do.dto.permission.PermissionDto;
import org.start2do.dto.permission.PermissionPageReq;
import org.start2do.ebean.service.AbsService;
import org.start2do.ebean.util.SysSettingUtil;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.SysPermissionRoleRef;
import org.start2do.entity.security.SysPermissionRoleRefId;
import org.start2do.entity.security.SysPermissionUserRef;
import org.start2do.entity.security.SysPermissionUserRefId;
import org.start2do.entity.security.query.QSysPermission;
import org.start2do.entity.security.query.QSysPermissionRoleRef;
import org.start2do.entity.security.query.QSysPermissionUserRef;
import org.start2do.service.IPermissionService;
import org.start2do.util.ListUtil;
import org.start2do.util.ListUtil.DiffDTO;
import org.start2do.util.Md5Util;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends AbsService<SysPermission> implements IPermissionService, CommandLineRunner {

    @Lazy
    @Resource
    private AbsPermissionController permissionController;

    public void reloadAllUrls() {
        Set<PermissionDto> urls = permissionController.getAllUrls();
        Set<String> set = new QSysPermission().findList().stream().map(SysPermission::getUrl)
            .collect(Collectors.toSet());
        DiffDTO<PermissionDto, String> dto = ListUtil.diff(urls, set,
            (permissionDto, s) -> permissionDto.getUrls().contains(s));
        for (PermissionDto t : dto.getAddList()) {
            for (String url : t.getUrls()) {
                String md5 = Md5Util.md5(url);
                new SysPermission(t.getGroupName(), url, t.isDefaultPass()).save();
                for (String initRoleCode : t.getInitRoleCodes()) {
                    new SysPermissionRoleRef(new SysPermissionRoleRefId(md5, initRoleCode)).save();
                }
                new SysPermissionRoleRef(new SysPermissionRoleRefId(md5,
                    SysSettingUtil.getLabel(Constant.TYPE_SYSTEM_SETTING, Constant.KEY_ADMIN_ROLE, "1"))).save();
            }
        }
        ListUtil.splitAfterRun(999, dto.getRemoveList(), spList -> {
            new QSysPermissionRoleRef().permissionId.in(spList).delete();
            new QSysPermissionUserRef().userId.in(spList).delete();
            new QSysPermission().id.in(spList).delete();
        });
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            reloadAllUrls();
        } catch (Exception e) {
            log.warn("权限表不存在", e);
        }
    }

    @Override
    @Transactional
    public boolean assignToUsers(String permissionId, List<String> userIds) {
        // 检查权限是否存在
        SysPermission permission = DB.find(SysPermission.class, permissionId);
        if (permission == null) {
            return false;
        }
        List<SysPermissionUserRef> list = new QSysPermissionUserRef().permissionId.eq(permissionId).findList();
        DiffDTO<String, SysPermissionUserRef> dto = ListUtil.diff(userIds, list,
            (s, sysPermissionRoleRef) -> sysPermissionRoleRef.getUserId().equals(s));
        for (String s : dto.getAddList()) {
            new SysPermissionUserRef(new SysPermissionUserRefId(permissionId, s)).save();
        }
        ListUtil.splitAfterRun(999, dto.getRemoveList(), spList -> {
            new QSysPermissionUserRef().permissionId.eq(permissionId).userId.in(
                spList.stream().map(SysPermissionUserRef::getUserId).collect(Collectors.toList())).delete();
        });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignToRoles(String permissionId, List<String> roleIds) {
        // 检查权限是否存在
        SysPermission permission = DB.find(SysPermission.class, permissionId);
        if (permission == null) {
            return false;
        }
        List<SysPermissionRoleRef> list = new QSysPermissionRoleRef().permissionId.eq(permissionId).findList();
        DiffDTO<String, SysPermissionRoleRef> dto = ListUtil.diff(roleIds, list,
            (s, sysPermissionRoleRef) -> sysPermissionRoleRef.getRoleId().equals(s));
        for (String s : dto.getAddList()) {
            new SysPermissionRoleRef(new SysPermissionRoleRefId(permissionId, s)).save();
        }
        ListUtil.splitAfterRun(999, dto.getRemoveList(), spList -> {
            new QSysPermissionRoleRef().permissionId.eq(permissionId).roleId.in(
                spList.stream().map(SysPermissionRoleRef::getRoleId).collect(Collectors.toList())).delete();
        });
        return true;
    }

    @Override
    @Transactional
    public boolean removeUserPermission(String permissionId, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        ListUtil.splitAfterRun(999, userIds, spList -> {
            new QSysPermissionUserRef().permissionId.eq(permissionId).userId.in(spList).delete();
        });
        return true;
    }

    @Override
    @Transactional
    public boolean removeRolePermission(String permissionId, List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        ListUtil.splitAfterRun(999, roleIds, spList -> {
            new QSysPermissionRoleRef().permissionId.eq(permissionId).roleId.in(spList).delete();
        });
        return true;
    }

    @Override
    public Page<SysPermission> page(PermissionPageReq request) {
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
        PagedList<SysPermission> pagedList = query.setFirstRow(request.getOffset()).setMaxRows(request.getSize())
            .findPagedList();

        return new Page<>(pagedList.getTotalCount(), request.getSize(), request.getCurrent(), pagedList.getList());
    }

    @Override
    public PermissionDetailResp getDetail(String id, boolean isUser) {
        // 查询权限
        QSysPermission q = new QSysPermission().id.eq(id);
        if (isUser) {
            q.users.fetch();
        } else {
            q.roles.fetch();
        }
        SysPermission permission = q.findOne();
        if (permission == null) {
            return null;
        }

        // 构建详情DTO
        PermissionDetailResp detailDto = new PermissionDetailResp()
            .setId(permission.getId())
            .setUrl(permission.getUrl())
            .setPass(permission.isPass())
            .setGroupName(permission.getGroupName());

        // 转换用户列表
        if (isUser && permission.getUsers() != null) {
            List<PermissionDetailResp.UserDto> userDtos = permission.getUsers().stream()
                .map(user -> new PermissionDetailResp.UserDto().setId(user.getId()).setUsername(user.getUsername()))
                .collect(Collectors.toList());
            detailDto.setUsers(userDtos);
        }

        // 转换角色列表
        if (!isUser && permission.getRoles() != null) {
            List<PermissionDetailResp.RoleDto> roleDtos = permission.getRoles().stream()
                .map(role -> new PermissionDetailResp.RoleDto().setId(role.getId()).setRoleName(role.getName()))
                .collect(Collectors.toList());
            detailDto.setRoles(roleDtos);
        }

        return detailDto;
    }
}
