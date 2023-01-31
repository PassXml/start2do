package org.start2do.controller;

import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.IdsReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.SysLogDtoMapper;
import org.start2do.dto.req.log.LogPageReq;
import org.start2do.dto.resp.log.LogPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.business.SysLog;
import org.start2do.entity.business.SysLog.Type;
import org.start2do.entity.business.query.QSysLog;
import org.start2do.service.SysLogService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.ExcelUtil;

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
            .notNull(req.getStartTime(), qClass.createTime::ge)
            .notNull(req.getEndTime(), qClass.createTime::lt);
        return R.ok(sysLogService.page(qClass, page, SysLogDtoMapper.INSTANCE::LogPageResp));
    }

    /**
     * 导出
     */
    @SneakyThrows
    @GetMapping("export")
    public void export(LogPageReq req, HttpServletResponse response) {
        QSysLog qClass = new QSysLog();
        Where.ready().notEmpty(req.getType(), s -> qClass.type.eq(Type.find(s)))
            .notNull(req.getStartTime(), qClass.createTime::ge)
            .notNull(req.getEndTime(), qClass.createTime::lt);
        List<SysLog> logs = sysLogService.findAll(qClass);
        response.reset();
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".csv");
        PrintWriter osw = response.getWriter();
        osw.write(new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}));
        osw.write(ExcelUtil.toCsv(logs, SysLog.class));
        osw.flush();
    }


    /**
     * 批量删除
     */
    @GetMapping("delete")
    public R delete(IdsReq req) {
        BeanValidatorUtil.validate(req);
        sysLogService.delete(new QSysLog().id.in(req.getId()));
        return R.ok();
    }
}
