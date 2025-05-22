package org.start2do.controller.servlet;

import io.ebean.annotation.Transactional;
import jakarta.validation.Valid;
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
import org.start2do.dto.BusinessException;
import org.start2do.dto.IdStrReq;
import org.start2do.dto.MenuResp;
import org.start2do.dto.MenuRespV2;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.dto.mapper.PositionDtoMapper;
import org.start2do.dto.req.position.PositionAddReq;
import org.start2do.dto.req.position.PositionPageReq;
import org.start2do.dto.req.position.PositionSetUserReq;
import org.start2do.dto.req.position.PositionUpdateReq;
import org.start2do.dto.resp.position.PositionDetailResp;
import org.start2do.dto.resp.position.PositionPageResp;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysPositionEntity;
import org.start2do.entity.security.SysPositionRefEntity;
import org.start2do.entity.security.SysPositionRefId;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.query.QSysPositionEntity;
import org.start2do.entity.security.query.QSysPositionRefEntity;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.servlet.SysPositionService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.TreeUtil;
import org.start2do.dto.Permission;

/** 岗位管理 */
@RestController
@RequiredArgsConstructor
@RequestMapping("position")
@ConditionalOnProperty(
    prefix = "start2do.business.controller",
    name = "position",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.SERVLET)
@Permission(groupName = "岗位管理")
public class SysPositionController {

  private final SysPositionService sysPositionService;

  /** 分页 */
  @GetMapping("page")
  public R<Page<PositionPageResp>> page(Page page, PositionPageReq req) {
    QSysPositionEntity qClass = new QSysPositionEntity();
    Where.ready()
        .like(req.getName(), qClass.name::like)
        .like(req.getCode(), qClass.code::like)
        .notNull(req.getStatus(), qClass.status::eq);
    return R.ok(
        sysPositionService.page(qClass, page, PositionDtoMapper.INSTANCE::toPositionPageResp));
  }

  /** 添加 */
  @SysLogSetting("添加岗位")
  @PostMapping("add")
  public R add(@RequestBody PositionAddReq req) {
    BeanValidatorUtil.validate(req);
    sysPositionService.save(PositionDtoMapper.INSTANCE.toEntity(req));
    return R.ok();
  }

  /** 更新 */
  @SysLogSetting("更新岗位")
  @PostMapping("update")
  public R update(@RequestBody PositionUpdateReq req) {
    BeanValidatorUtil.validate(req);
    SysPositionEntity position = sysPositionService.getById(req.getId());
    if (position == null) {
      throw new BusinessException("岗位不存在");
    }
    PositionDtoMapper.INSTANCE.update(position, req);
    sysPositionService.update(position);
    return R.ok();
  }

  /** 删除 */
  @SysLogSetting("删除岗位")
  @GetMapping("delete")
  public R delete(IdStrReq req) {
    BeanValidatorUtil.validate(req);
    List<PositionPageResp> positionPageResps =
        TreeUtil.generateTreesNoMiss(
            sysPositionService.findAll().stream()
                .map(PositionDtoMapper.INSTANCE::toPositionPageResp)
                .toList());
    PositionPageResp node = TreeUtil.findNode(positionPageResps, req.getId());
    if (!node.getAllChildrenId().isEmpty()) {
      throw new BusinessException("存在子节点无法删除");
    }

    sysPositionService.deleteById(req.getId());
    return R.ok();
  }

  /** 详情 */
  @GetMapping("detail")
  public R<PositionDetailResp> detail(IdStrReq req) {
    BeanValidatorUtil.validate(req);
    SysPositionEntity position = sysPositionService.getById(req.getId());
    if (position == null) {
      throw new BusinessException("岗位不存在");
    }
    return R.ok(PositionDtoMapper.INSTANCE.toPositionDetailResp(position));
  }

  /** 菜单 获取所有启用的岗位，并按排序号、名称排序 */
  @GetMapping("menu")
  public R<List<MenuResp>> menu() {
    List<SysPositionEntity> positions =
        sysPositionService.findAll(
            new QSysPositionEntity()
                .status
                .eq(EnableType.Enable) // 只查询启用的岗位
                .orderBy()
                .sort
                .asc() // 按sort字段升序
                .name
                .asc() // 按name字段升序
            );
    return R.ok(
        positions.stream()
            .map(position -> new MenuResp(position.getName(), position.getId()))
            .toList());
  }

  /** 菜单 获取所有启用的岗位，并按排序号、名称排序 */
  @GetMapping("tree")
  public R<List<PositionPageResp>> tree() {
    List<SysPositionEntity> positions =
        sysPositionService.findAll(
            new QSysPositionEntity()
                .status
                .eq(EnableType.Enable) // 只查询启用的岗位
                .orderBy()
                .sort
                .asc() // 按sort字段升序
                .name
                .asc() // 按name字段升序
            );
    return R.ok(
        TreeUtil.generateTreesNoMiss(
            positions.stream().map(PositionDtoMapper.INSTANCE::toPositionPageResp).toList()));
  }

  /** 设置岗位用户 */
  @PostMapping("/setUser")
  @Transactional
  public R setPosition(@Valid @RequestBody PositionSetUserReq req) {
    new QSysPositionRefEntity().postId.eq(req.getPositionId()).delete();
    for (String s : req.getUserId()) {
      new SysPositionRefEntity(new SysPositionRefId(s, req.getPositionId())).save();
    }
    return R.ok();
  }

  /** 查看岗位用户 */
  @GetMapping("/users")
  public R<List<MenuRespV2>> setPosition(@Valid IdStrReq req) {
    List<SysUser> users =
        new QSysUser()
            .id
            .in(
                new QSysPositionRefEntity()
                    .select(QSysPositionRefEntity.alias().userId)
                    .postId
                    .eq(req.getId())
                    .query())
            .findList();
    return R.ok(
        users.stream()
            .map(t -> new MenuRespV2(t.getRealName(), t.getId(), t.getUsername()))
            .toList());
  }
}
