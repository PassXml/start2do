package org.start2do.controller.webflux;

import io.ebean.Model;
import io.ebean.typequery.QueryBean;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.ebean.service.AbsMixService;

@ConditionalOnWebApplication(type = Type.REACTIVE)
public abstract class AbsBaseController<Entity extends Model, QClass extends QueryBean<Entity, QClass>, SERVICE extends AbsMixService<Entity, T>, T, PageReq extends Page, PageResp, AddReq, IDReq, DetailResp> {


    @Lazy
    @Autowired
    protected SERVICE service;

    protected abstract QClass pageQClass(PageReq req);

    protected abstract PageResp toPageResp(Entity entity);

    protected abstract Entity toEntity(AddReq add);

    protected abstract QClass toDelete(IDReq req);

    protected abstract Entity updateEntity(AddReq edit);

    protected abstract DetailResp toDetail(Entity entity);

    protected abstract QClass detailQClass(IDReq req);

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<PageResp>> page(@Valid PageReq req) {
        return R.ok(service.page(pageQClass(req), req, this::toPageResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@Valid @RequestBody AddReq req) {
        service.save(toEntity(req));
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<DetailResp> detail(@Valid IDReq req) {
        return R.ok(toDetail(service.getOne(detailQClass(req))));
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@Valid @RequestBody AddReq req) {
        service.update(updateEntity(req));
        return R.ok();
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(@Valid IDReq req) {
        service.delete(toDelete(req));
        return R.ok();
    }

}
