package org.start2do.service;

import io.ebean.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.util.ListUtil;

@Service
@RequiredArgsConstructor
public class SysUserService extends AbsService<SysUser> {

    private final SysLoginRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public void add(SysUser entity, List<Integer> roles) {
        checkRole(roles);
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        save(entity);
        for (Integer roleId : roles) {
            sysUserRoleService.save(new SysUserRole(entity.getId(), roleId));
        }
    }

    private void checkRole(List<Integer> roles) {
        List<SysRole> list = sysRoleService.findAll(new QSysRole().id.in(roles));
        if (list.size() != roles.size()) {
            throw new BusinessException("用户组错误");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Integer id) {
        new QSysUserRole().userId.eq(id).delete();
        deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user, List<Integer> roles) {
        checkRole(roles);
        this.update(user);
        List<SysUserRole> userRoles = sysUserRoleService.findAll(new QSysUserRole().userId.eq(user.getId()));
        ListUtil.diff(roles, userRoles, (integer, sysRole) -> sysRole.getRoleId().equals(integer), integers -> {
            for (Integer integer : integers) {
                sysUserRoleService.save(new SysUserRole(user.getId(), integer));
            }
        }, null, sysUserRoles -> {
            sysUserRoleService.delete(new QSysUserRole().userId.eq(user.getId()).roleId.in(
                sysUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet())
            ));
        });
    }

    public void checkUserName(String username) {
        if (count(new QSysUser().username.eq(username)) > 0) {
            throw new BusinessException("用户名已被使用");
        }
    }
}
