package org.start2do.controller.webflux;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.IdReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.MenuDtoMapper;
import org.start2do.dto.req.menu.MenuAddReq;
import org.start2do.dto.req.menu.MenuPageReq;
import org.start2do.dto.req.menu.MenuUpdateReq;
import org.start2do.dto.resp.menu.MenuDetailResp;
import org.start2do.dto.resp.menu.MenuPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.service.webflux.SysMenuReactiveService;
import org.start2do.util.BeanValidatorUtil;
import reactor.core.publisher.Mono;

/**
 * 菜单管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("menu")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "menu", havingValue = "true")
@ConditionalOnWebApplication(type = Type.REACTIVE)

public class SysMenuController {

    private final SysMenuReactiveService sysMenuService;

    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<MenuPageResp>>> page(Page page, MenuPageReq req) {
        QSysMenu qClass = new QSysMenu();
        Where.ready().like(req.getName(), qClass.name::like);
        return sysMenuService.page(qClass, page, MenuDtoMapper.INSTANCE::toMenuPageResp).map(R::ok);
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public Mono<R<Boolean>> add(@RequestBody MenuAddReq req) {
        BeanValidatorUtil.validate(req);
        return sysMenuService.save(MenuDtoMapper.INSTANCE.toEntity(req)).map(menu -> true).map(R::ok);
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public Mono<R<Boolean>> update(@RequestBody MenuUpdateReq req) {
        BeanValidatorUtil.validate(req);
        return sysMenuService.getById(req.getId()).flatMap(menu -> {
            MenuDtoMapper.INSTANCE.update(menu, req);
            return sysMenuService.update(menu);
        }).map(menu -> true).map(R::ok);
    }

    /**
     * 添加
     */
    @GetMapping("delete")
    public Mono<R<Boolean>> delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        return sysMenuService.remove(req.getId()).map(R::ok);
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<MenuDetailResp>> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        return sysMenuService.getById(req.getId()).map(MenuDtoMapper.INSTANCE::toMenuDetailResp).map(R::ok);
    }

    /**
     * 角色-菜单
     */
    @GetMapping("menu/role")
    public Mono<R<List<MenuDetailResp>>> menuByRole(IdReq req) {
        BeanValidatorUtil.validate(req);
        return sysMenuService.findAll(new QSysMenu().roles.id.eq(req.getId())).map(sysMenus -> {
            return sysMenus.stream().map(MenuDtoMapper.INSTANCE::toMenuDetailResp).toList();
        }).map(R::ok);
    }

    /**
     * 所有菜单
     */
    @GetMapping("menu/all")
    public Mono<R<List<MenuDetailResp>>> menuAll(MenuPageReq req) {
        QSysMenu qClass = new QSysMenu();
        Where.ready().like(req.getName(), qClass.name);
        return sysMenuService.findAllUseCache(qClass)
            .map(sysMenus -> sysMenus.stream().map(MenuDtoMapper.INSTANCE::toMenuDetailResp).toList()).map(R::ok);
    }

}
