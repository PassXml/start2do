package org.start2do.controller.servlet;

import io.ebean.Model;
import io.ebean.typequery.TQRootBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.ebean.service.AbsService;
import org.start2do.util.BeanValidatorUtil;

@ConditionalOnWebApplication(type = Type.SERVLET)
public abstract class BaseController<Entity extends Model, QClass extends TQRootBean<Entity, QClass>, SERVICE extends AbsService<Entity>, PageResp, PageReq, Add, Edit, Delete, DetailReq, DetailResp> {

    @Autowired
    @Lazy
    protected SERVICE service;

    protected abstract QClass pageQClass(PageReq req);

    protected abstract PageResp toPageResp(Entity entity);

    protected abstract Entity toEntity(Add add);

    protected abstract QClass toDelete(Delete req);

    protected abstract Entity updateEntity(Edit edit);

    protected abstract DetailResp toDetail(Entity entity);

    protected abstract QClass detailQClass(DetailReq req);

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<PageResp>> page(Page page, PageReq req) {
        BeanValidatorUtil.validate(req);
        return R.ok(service.page(pageQClass(req), page, this::toPageResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody Add req) {
        BeanValidatorUtil.validate(req);
        service.save(toEntity(req));
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<DetailResp> detail(DetailReq req) {
        BeanValidatorUtil.validate(req);
        return R.ok(toDetail(service.getOne(detailQClass(req))));
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody Edit req) {
        BeanValidatorUtil.validate(req);
        service.update(updateEntity(req));
        return R.ok();
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(Delete req) {
        BeanValidatorUtil.validate(req);
        service.delete(toDelete(req));
        return R.ok();
    }
}
