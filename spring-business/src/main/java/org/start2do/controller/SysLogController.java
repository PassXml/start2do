package org.start2do.controller;

import com.alibaba.excel.EasyExcel;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.IdsReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.SysLogDtoMapper;
import org.start2do.dto.req.log.LogPageReq;
import org.start2do.dto.resp.log.LogExcelPojo;
import org.start2do.dto.resp.log.LogPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.business.SysLog;
import org.start2do.entity.business.SysLog.Type;
import org.start2do.entity.business.query.QSysLog;
import org.start2do.service.SysLogService;
import org.start2do.util.BeanValidatorUtil;

/**
 * 日志管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class SysLogController {

    private final SysLogService sysLogService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<LogPageResp>> page(Page page, LogPageReq req) {
        QSysLog qClass = new QSysLog().createTime.desc();
        Where.ready().notEmpty(req.getType(), s -> qClass.type.eq(Type.find(s)))
            .notNull(req.getStartTime(), qClass.createTime::ge).notNull(req.getEndTime(), qClass.createTime::lt);
        return R.ok(sysLogService.page(qClass, page, SysLogDtoMapper.INSTANCE::LogPageResp));
    }

    /**
     * 导出
     */
    @GetMapping("export")
    public void export(LogPageReq req, HttpServletResponse response) throws IOException {
        QSysLog qClass = new QSysLog().createTime.desc();
        Where.ready().notEmpty(req.getType(), s -> qClass.type.eq(Type.find(s)))
            .notNull(req.getStartTime(), qClass.createTime::ge).notNull(req.getEndTime(), qClass.createTime::lt);
        List<SysLog> logs = sysLogService.findAll(qClass);
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        EasyExcel.write(outputStream, LogExcelPojo.class).sheet("系统日志")
            .doWrite(logs.stream().map(SysLogDtoMapper.INSTANCE::toLogExcelPojo).collect(Collectors.toList()));
        outputStream.flush();
    }


    /**
     * 批量删除
     */
    @GetMapping("delete")
    @DeleteMapping("delete")
    public R delete(IdsReq req) {
        BeanValidatorUtil.validate(req);
        sysLogService.delete(new QSysLog().id.in(req.getId()));
        return R.ok();
    }
}
