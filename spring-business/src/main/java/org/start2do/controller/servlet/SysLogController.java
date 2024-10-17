package org.start2do.controller.servlet;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.BusinessException;
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
import org.start2do.service.servlet.SysLogService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.ListUtil;

/**
 * 日志管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "log", havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SysLogController {

    private final SysLogService sysLogService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<LogPageResp>> page(Page page, LogPageReq req) {
        QSysLog qClass = new QSysLog().createTime.desc();
        Where.ready().notEmpty(req.getType(), s -> qClass.type.eq(Type.find(s)))
            .notNull(req.getStartTime(), qClass.createTime::ge).notNull(req.getEndTime(), qClass.createTime::lt)
            .notEmpty(req.getKeyword(), s -> {
                qClass.or().title.like("%" + s + "%").or().createPerson.like("%" + s + "%").or().responseBody.like(
                    "%" + s + "%");
            });

        return R.ok(sysLogService.page(qClass, page, SysLogDtoMapper.INSTANCE::LogPageResp));
    }

    /**
     * 导出
     */
    @GetMapping("export")
    public void export(LogPageReq req, HttpServletResponse response) throws IOException {
        QSysLog qClass = new QSysLog().createTime.desc();
        Where.ready().notEmpty(req.getType(), s -> qClass.type.eq(Type.find(s)));
        if (ListUtil.isEmpty(req.getIds())) {
            LocalDateTime now = LocalDateTime.now();
            if (req.getTimeRange() == null) {
                if (req.getStartTime() != null && req.getEndTime() != null) {
                    if (ChronoUnit.DAYS.between(req.getStartTime(), req.getEndTime()) > 90) {
                        throw new BusinessException("不能导出大于90天的数据");
                    }
                }
                if (req.getStartTime() != null) {
                    if (ChronoUnit.DAYS.between(req.getStartTime(), now) > 90) {
                        throw new BusinessException("不能导出大于90天的数据");
                    }
                    qClass.createTime.ge(req.getStartTime());
                } else {
                    qClass.createTime.ge(now.plusDays(-7));
                }
                if (req.getEndTime() == null) {

                } else {
                    qClass.createTime.lt(now);
                }
            } else {
                LocalDateTime endTime = req.getTimeRange()[1];
                LocalDateTime startTime = req.getTimeRange()[0];
                if (ChronoUnit.DAYS.between(startTime, endTime) > 90) {
                    throw new BusinessException("不能导出大于90天的数据");
                }
                qClass.createTime.ge(startTime).createTime.lt(endTime);
            }
        } else {
            if (req.getIds().size() > 999) {
                throw new BusinessException("指定导出,导出条数最大999条");
            }
            qClass.id.inOrEmpty(req.getIds());
        }
        List<SysLog> logs = sysLogService.findAll(qClass);
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        List<LogExcelPojo> pojos = logs.stream().map(SysLogDtoMapper.INSTANCE::toLogExcelPojo)
            .collect(Collectors.toList());
        EasyExcel.write(outputStream, LogExcelPojo.class)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).sheet("系统日志").doWrite(pojos);
        outputStream.flush();
        outputStream.close();
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
