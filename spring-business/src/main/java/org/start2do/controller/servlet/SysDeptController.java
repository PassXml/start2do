package org.start2do.controller.servlet;

import java.util.List;
import java.util.Map;
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
import org.start2do.dto.MenuResp;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.DeptDtoMapper;
import org.start2do.dto.req.dept.DeptAddReq;
import org.start2do.dto.req.dept.DeptPageReq;
import org.start2do.dto.req.dept.DeptUpdateReq;
import org.start2do.dto.resp.dept.DeptDetailResp;
import org.start2do.dto.resp.dept.DeptPageResp;
import org.start2do.dto.resp.dept.DeptTreeResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.query.QSysDept;
import org.start2do.service.servlet.SysDeptService;
import org.start2do.util.BeanValidatorUtil;

/**
 * 部门管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("dept")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "dept", havingValue = "true")
@ConditionalOnWebApplication(type = Type.SERVLET)

public class SysDeptController {

    private final SysDeptService sysDeptService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<DeptPageResp>> page(Page page, DeptPageReq req) {
        QSysDept qClass = new QSysDept().sort.desc();
        Where.ready().like(req.getName(), qClass.name);
        return R.ok(sysDeptService.page(qClass, page, DeptDtoMapper.INSTANCE::toDeptPageResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody DeptAddReq req) {
        BeanValidatorUtil.validate(req);
        sysDeptService.save(DeptDtoMapper.INSTANCE.toEntity(req));
        return R.ok();
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody DeptUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysDept dept = sysDeptService.getById(req.getId());
        DeptDtoMapper.INSTANCE.update(dept, req);
        sysDeptService.update(dept);
        return R.ok();
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        sysDeptService.remove(req.getId());
        return R.ok();
    }

    /**
     * 部门菜单
     */
    @GetMapping("menu/dept")
    public R<List<MenuResp>> menu() {
        return R.ok(sysDeptService.findAll().stream().map(sysDept -> new MenuResp(sysDept.getName(), sysDept.getId()))
            .collect(Collectors.toList())
        );
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<DeptDetailResp> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        return R.ok(DeptDtoMapper.INSTANCE.toDeptDetailResp(sysDeptService.getById(req.getId())));
    }

    @GetMapping("tree")
    public R<List<DeptTreeResp>> tree() {
        List<SysDept> depts = sysDeptService.findAll();
        List<DeptTreeResp> objects = depts.stream().map(DeptDtoMapper.INSTANCE::toDeptTreeResp)
            .collect(Collectors.toList());
        Map<Integer, List<DeptTreeResp>> map = objects.stream().filter(t -> t.getParentId() != null)
            .collect(Collectors.groupingBy(DeptTreeResp::getParentId));
        for (DeptTreeResp object : objects) {
            object.setChildren(map.get(object.getId()));
        }
        return R.ok(
            objects.stream().filter(t -> t.getParentId() == null).collect(Collectors.toList()));
    }

}
