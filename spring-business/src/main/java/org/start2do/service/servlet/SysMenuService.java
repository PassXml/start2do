package org.start2do.service.servlet;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.entity.security.query.QSysRoleMenu;


@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.service", name = "menu", havingValue = "true")
public class SysMenuService extends AbsService<SysMenu> {

    private final SysRoleMenuService sysRoleMenuService;

    public void remove(Integer id) {
        if (sysRoleMenuService.count(new QSysRoleMenu().id.menuId.eq(id)) > 0) {
            throw new BusinessException("请先取消对应权限");
        }
        if (count(new QSysMenu().parentId.eq(id)) > 0) {
            throw new BusinessException("请先删除子菜单");
        }
        super.deleteById(id);
    }
}
