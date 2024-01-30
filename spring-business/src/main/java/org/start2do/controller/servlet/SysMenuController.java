package org.start2do.controller.servlet;

import java.util.List;
import java.util.stream.Collectors;
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
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.service.servlet.SysMenuService;
import org.start2do.util.BeanValidatorUtil;

/**
 * 菜单管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("menu")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "menu", havingValue = "true")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<MenuPageResp>> page(Page page, MenuPageReq req) {
        QSysMenu qClass = new QSysMenu();
        Where.ready().like(req.getName(), qClass.name::like);
        return R.ok(sysMenuService.page(qClass, page, MenuDtoMapper.INSTANCE::toMenuPageResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody MenuAddReq req) {
        BeanValidatorUtil.validate(req);
        sysMenuService.save(MenuDtoMapper.INSTANCE.toEntity(req));
        return R.ok();
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody MenuUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysMenu menu = sysMenuService.getById(req.getId());
        MenuDtoMapper.INSTANCE.update(menu, req);
        sysMenuService.update(menu);
        return R.ok();
    }

    /**
     * 添加
     */
    @GetMapping("delete")
    public R delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        sysMenuService.remove(req.getId());
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<MenuDetailResp> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        SysMenu menu = sysMenuService.getById(req.getId());
        return R.ok(MenuDtoMapper.INSTANCE.toMenuDetailResp(menu));
    }

    /**
     * 角色-菜单
     */
    @GetMapping("menu/role")
    public R<List<MenuDetailResp>> menuByRole(IdReq req) {
        BeanValidatorUtil.validate(req);
        return R.ok(sysMenuService.findAll(new QSysMenu().roles.id.eq(req.getId())).stream()
            .map(MenuDtoMapper.INSTANCE::toMenuDetailResp).collect(Collectors.toList()));
    }

    /**
     * 所有菜单
     */
    @GetMapping("menu/all")
    public R<List<MenuDetailResp>> menuAll(MenuPageReq req) {
        QSysMenu qClass = new QSysMenu();
        Where.ready().notEmpty(req.getName(), qClass.name::like);
        return R.ok(sysMenuService.findAllUseCache(qClass).stream()
            .map(MenuDtoMapper.INSTANCE::toMenuDetailResp).collect(Collectors.toList()));
    }

}
