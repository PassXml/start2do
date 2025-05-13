package org.start2do.controller.servlet;

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
import org.start2do.dto.IdStrReq;
import org.start2do.dto.MenuResp;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.dto.mapper.PositionDtoMapper;
import org.start2do.dto.req.position.PositionAddReq;
import org.start2do.dto.req.position.PositionPageReq;
import org.start2do.dto.req.position.PositionUpdateReq;
import org.start2do.dto.resp.position.PositionDetailResp;
import org.start2do.dto.resp.position.PositionPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysPositionEntity;
import org.start2do.entity.security.query.QSysPositionEntity;
import org.start2do.service.servlet.SysPositionService;
import org.start2do.util.BeanValidatorUtil;

/**
 * 岗位管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("position")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "position", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysPositionController {

    private final SysPositionService sysPositionService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<PositionPageResp>> page(Page page, PositionPageReq req) {
        QSysPositionEntity qClass = new QSysPositionEntity();
        Where.ready()
            .like(req.getName(), qClass.name::like)
            .like(req.getCode(), qClass.code::like)
            .eq(req.getStatus(), qClass.status::eq);
        return R.ok(sysPositionService.page(qClass, page, PositionDtoMapper.INSTANCE::toPositionPageResp));
    }

    /**
     * 添加
     */
    @SysLogSetting("添加岗位")
    @PostMapping("add")
    public R add(@RequestBody PositionAddReq req) {
        BeanValidatorUtil.validate(req);
        sysPositionService.save(PositionDtoMapper.INSTANCE.toEntity(req));
        return R.ok();
    }

    /**
     * 更新
     */
    @SysLogSetting("更新岗位")
    @PostMapping("update")
    public R update(@RequestBody PositionUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysPositionEntity position = sysPositionService.getById(req.getId());
        if (position == null) {
            return R.fail("岗位不存在");
        }
        PositionDtoMapper.INSTANCE.update(position, req);
        sysPositionService.update(position);
        return R.ok();
    }

    /**
     * 删除
     */
    @SysLogSetting("删除岗位")
    @GetMapping("delete")
    public R delete(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        // 实际项目中，可能需要检查岗位是否被引用或是否有子岗位
        sysPositionService.remove(req.getId());
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<PositionDetailResp> detail(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        SysPositionEntity position = sysPositionService.getById(req.getId());
        if (position == null) {
            return R.fail("岗位不存在");
        }
        return R.ok(PositionDtoMapper.INSTANCE.toPositionDetailResp(position));
    }

    /**
     * 菜单
     * 获取所有启用的岗位，并按排序号、名称排序
     */
    @GetMapping("menu")
    public R<List<MenuResp>> menu() {
        List<SysPositionEntity> positions = sysPositionService.findAll(
            new QSysPositionEntity()
                .status.eq(org.start2do.ebean.dto.EnableType.ENABLE) // 只查询启用的岗位
                .orderBy().sort.asc() // 按sort字段升序
                .name.asc() // 按name字段升序
        );
        return R.ok(
            positions.stream()
                .map(position -> new MenuResp(position.getName(), position.getId()))
                .toList()
        );
    }
}
