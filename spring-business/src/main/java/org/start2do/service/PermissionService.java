package org.start2do.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.start2do.controller.AbsPermissionController;
import org.start2do.dto.permission.PermissionDto;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.SysPermissionRoleRef;
import org.start2do.entity.security.SysPermissionRoleRefId;
import org.start2do.entity.security.SysPermissionUserRefId;
import org.start2do.entity.security.query.QSysPermission;
import org.start2do.entity.security.query.QSysPermissionRoleRef;
import org.start2do.entity.security.query.QSysPermissionUserRef;
import org.start2do.util.ListUtil;
import org.start2do.util.Md5Util;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService extends AbsService<SysPermission> implements CommandLineRunner {

    private final AbsPermissionController permissionController;

    public void removeUserPermission(@NotNull String userId, @NotEmpty String permissionId) {
        new QSysPermissionUserRef().id.eq(new SysPermissionUserRefId(permissionId, userId)).delete();
    }

    public void addRolePermission(String roleId, String permissionId) {
        new SysPermissionRoleRef(new SysPermissionRoleRefId(permissionId, roleId)).save();
    }


    public void deleteRolePermission(String roleId, String permissionId) {
        new QSysPermissionRoleRef().id.eq(new SysPermissionRoleRefId(permissionId, roleId)).delete();
    }

    public void reloadAllUrls() {
        Set<PermissionDto> urls = permissionController.getAllUrls();
        Set<String> set = new QSysPermission().findList().stream().map(SysPermission::getId)
            .collect(Collectors.toSet());
        for (PermissionDto t : urls) {
            java.util.List<String> updateUrls = new ArrayList<>();
            for (String url : t.getUrls()) {
                String md5 = Md5Util.md5(url);
                if (set.contains(md5)) {
                    updateUrls.add(md5);
                    continue;
                }
                new SysPermission().setUrl(url).setPass(t.isDefaultPass()).setId(md5).save();
            }
            ListUtil.splitAfterRun(999, updateUrls, spList -> {
                new QSysPermission().asUpdate().set(QSysPermission.alias().pass.toString(), t.isDefaultPass()).where()
                    .idIn(spList)
                    .update();
            });
        }
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            reloadAllUrls();
        } catch (Exception e) {
            log.warn("权限表不存在", e);
        }
    }
}
