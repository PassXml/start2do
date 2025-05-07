package org.start2do.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.start2do.controller.AbsPermissionController;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysPermission;
import org.start2do.entity.security.SysPermissionRoleRef;
import org.start2do.entity.security.SysPermissionRoleRefId;
import org.start2do.entity.security.SysPermissionUserRefId;
import org.start2do.entity.security.query.QSysPermissionRoleRef;
import org.start2do.entity.security.query.QSysPermissionUserRef;

@Service
@RequiredArgsConstructor
public class PermissionService extends AbsService<SysPermission> implements CommandLineRunner {

    private final AbsPermissionController permissionController;

    public void removeUserPermission(@NotNull Integer userId, @NotEmpty String permissionId) {
        new QSysPermissionUserRef().id.eq(new SysPermissionUserRefId(permissionId, userId)).delete();
    }

    public void addRolePermission(Integer roleId, String permissionId) {
        new SysPermissionRoleRef(new SysPermissionRoleRefId(permissionId, roleId)).save();
    }


    public void deleteRolePermission(Integer roleId, String permissionId) {
        new QSysPermissionRoleRef().id.eq(new SysPermissionRoleRefId(permissionId, roleId)).delete();
    }

    public void reloadAllUrls() {
        Set<String> urls = permissionController.getAllUrls();
        for (String url : urls) {
            new SysPermission(url).save();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        reloadAllUrls();
    }
}
