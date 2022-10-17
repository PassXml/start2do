package org.start2do.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysRoleMenu;
import org.start2do.entity.security.SysRoleMenuId;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysRoleMenu;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.util.ListUtil;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})

public class SysRoleService extends AbsService<SysRole> {

    private final SysRoleMenuService sysRoleMenuService;
    private final SysMenuService sysMenuService;
    private final SysUserService sysUserService;

    public void remove(Integer id) {
        if (sysMenuService.count(new QSysMenu().roles.id.eq(id)) > 0) {
            throw new BusinessException("请先取消权限");
        }
        if (sysUserService.count(new QSysUser().roles.id.eq(id)) > 0) {
            throw new BusinessException("用户组用户不为空");
        }
        deleteById(id);
    }

    public void set(Integer roleId, List<Integer> menuIds) {
        if (menuIds != null && !menuIds.isEmpty()) {
            if (sysMenuService.count(new QSysMenu().id.in(menuIds)) != menuIds.size()) {
                throw new BusinessException("数据有误,请刷新页面");
            }
        }
        if (count(new QSysRole().id.eq(roleId)) < 1) {
            throw new BusinessException("数据有误,请刷新页面");
        }
        List<SysRoleMenu> menus = sysRoleMenuService.findAll(new QSysRoleMenu().id.roleId.eq(roleId));
        ListUtil.diff(
            menuIds, menus, (integer, sysRoleMenu) -> integer.equals(sysRoleMenu.getId().getMenuId()), integers -> {
                for (Integer integer : integers) {
                    sysRoleMenuService.save(new SysRoleMenu(new SysRoleMenuId(
                        roleId, integer
                    )));
                }
            }, null, integers -> {
                Set<Integer> collect = integers.stream().map(SysRoleMenu::getId).map(SysRoleMenuId::getMenuId)
                    .collect(Collectors.toSet());
                sysRoleMenuService.delete(new QSysRoleMenu().id.roleId.eq(roleId).id.menuId.in(collect));
            }
        );
    }
}
