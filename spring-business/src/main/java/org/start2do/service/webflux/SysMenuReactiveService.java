package org.start2do.service.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsMixService;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.entity.security.query.QSysRoleMenu;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "menu", havingValue = "true",matchIfMissing = true)
public class SysMenuReactiveService extends AbsMixService<SysMenu, String> {

    private final SysRoleMenuReactiveService sysRoleMenuService;

    public Mono<Boolean> remove(String id) {
        return sysRoleMenuService.countReactive(new QSysRoleMenu().id.menuId.eq(id)).filter(integer -> integer <= 0)
            .switchIfEmpty(Mono.error(new BusinessException("请先取消对应权限"))).then(
                countReactive(new QSysMenu().parentId.eq(id)).filter(integer -> integer <= 0)
                    .switchIfEmpty(Mono.error(new BusinessException("请先删除子菜单"))))
            .then(deleteByIdReactive(id));
    }
}
