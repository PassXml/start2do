package org.start2do.controller.webflux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.start2do.service.webflux.SysDeptReactiveService;
import org.start2do.util.BeanValidatorUtil;
import reactor.core.publisher.Mono;

/**
 * 部门管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("dept")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "dept", havingValue = "true")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysDeptController {

    private final SysDeptReactiveService sysDeptService;

    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<DeptPageResp>>> page(Page page, DeptPageReq req) {
        return Mono.fromSupplier(() -> {
            QSysDept qClass = new QSysDept().sort.desc();
            Where.ready().like(req.getName(), qClass.name);
            return qClass;
        }).flatMap(qClass -> sysDeptService.page(qClass, page, DeptDtoMapper.INSTANCE::toDeptPageResp)).map(R::ok);
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public Mono<R<Integer>> add(@RequestBody DeptAddReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.just(DeptDtoMapper.INSTANCE.toEntity(req)).flatMap(sysDeptService::save).filter(Objects::nonNull)
            .map(SysDept::getId).map(R::ok);
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public Mono<R<Boolean>> update(@RequestBody DeptUpdateReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.from(sysDeptService.getById(req.getId())).flatMap(sysDept -> {
            DeptDtoMapper.INSTANCE.update(sysDept, req);
            return sysDeptService.update(sysDept).map(sysDept1 -> true);
        }).map(R::ok);
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public Mono<R<Boolean>> delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.just(req.getId()).flatMap(sysDeptService::remove).map(R::ok);
    }

    /**
     * 部门菜单
     */
    @GetMapping("menu/dept")
    public Mono<List<MenuResp>> menu() {
        return Mono.from(sysDeptService.findAll()).map(
            sysDepts -> sysDepts.stream().map(sysDept -> new MenuResp(sysDept.getName(), sysDept.getId())).toList());
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<DeptDetailResp>> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.from(sysDeptService.getById(req.getId()))
            .map(DeptDtoMapper.INSTANCE::toDeptDetailResp).map(R::ok);
    }

    @GetMapping("tree")
    public Mono<R<List<DeptTreeResp>>> tree() {
        return Mono.from(sysDeptService.findAll()).map(depts -> {
            List<DeptTreeResp> objects = depts.stream().map(DeptDtoMapper.INSTANCE::toDeptTreeResp).toList();
            Map<Integer, List<DeptTreeResp>> map = objects.stream().filter(t -> t.getParentId() != null)
                .collect(Collectors.groupingBy(DeptTreeResp::getParentId));
            for (DeptTreeResp object : objects) {
                object.setChildren(map.get(object.getId()));
            }
            return objects.stream().filter(t -> t.getParentId() == null).toList();
        }).map(R::ok);
    }

}
