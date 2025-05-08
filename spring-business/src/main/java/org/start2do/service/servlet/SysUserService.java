package org.start2do.service.servlet;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUserDept;
import org.start2do.entity.security.SysUserDeptId;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysDept;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.entity.security.query.QSysUserDept;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.service.SysLoginRoleService;
import org.start2do.util.ListUtil;
import org.start2do.util.spring.RedisCacheUtil;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "user", havingValue = "true", matchIfMissing = true)
public class SysUserService extends AbsService<SysUser> {

    private final SysLoginRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public void add(SysUser entity, String mainDept, List<String> roles) {
        checkRole(roles);
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        save(entity);
        for (String roleId : roles) {
            sysUserRoleService.save(new SysUserRole(entity.getId(), roleId));
        }
        new SysUserDept(new SysUserDeptId(entity.getId(), mainDept), SysUserDept.Type.Main).save();
    }

    public SysUser getRedisCacheById(Long id) {
        return RedisCacheUtil.get("Cache:SysUser:" + id, () -> findOneById(id));
    }

    private void checkRole(List<String> roles) {
        List<SysRole> list = sysRoleService.findAll(new QSysRole().id.in(roles));
        if (list.size() != roles.size()) {
            throw new BusinessException("用户组错误");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(String id) {
        new QSysUserDept().userId.eq(id).delete();
        new QSysUserRole().userId.eq(id).delete();
        deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user, String mainDeptId, List<String> roles) {
        checkRole(roles);
        this.update(user);
        SysDept dept = user.getMainDept();
        if (dept == null) {
            new SysUserDept(new SysUserDeptId(user.getId(), mainDeptId), SysUserDept.Type.Main).save();
        } else {
            if (!dept.getId().equals(mainDeptId)) {
                new QSysUserDept().asUpdate().set(QSysUserDept.alias().type, SysUserDept.Type.Sub).where()
                    .idEq(new SysUserDeptId(user.getId(), dept.getId())).update();
                new SysUserDept(new SysUserDeptId(user.getId(), mainDeptId), SysUserDept.Type.Main).save();
            }
        }
        List<SysUserRole> userRoles = sysUserRoleService.findAll(new QSysUserRole().userId.eq(user.getId()));
        ListUtil.diff(roles, userRoles, (integer, sysRole) -> sysRole.getRoleId().equals(integer), integers -> {
            for (String integer : integers) {
                sysUserRoleService.save(new SysUserRole(user.getId(), integer));
            }
        }, null, sysUserRoles -> {
            sysUserRoleService.delete(new QSysUserRole().userId.eq(user.getId()).roleId.in(
                sysUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet())
            ));
        });
        //更新用户组

    }


    public void checkUserName(String username) {
        if (count(new QSysUser().username.eq(username)) > 0) {
            throw new BusinessException("用户名已被使用");
        }
    }
}
