package org.start2do.controller.webflux;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
import org.start2do.entity.business.SysLog.Type;
import org.start2do.entity.business.query.QSysLog;
import org.start2do.service.webflux.SysLogReactiveService;
import org.start2do.util.BeanValidatorUtil;
import reactor.core.publisher.Mono;

/**
 * 日志管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "log", havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)

public class SysLogController {

    public static Integer MIN_DAY = 90;
    private final SysLogReactiveService sysLogService;

    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<LogPageResp>>> page(Page page, LogPageReq req) {
        QSysLog qClass = new QSysLog().createTime.desc();
        Where.ready().notEmpty(req.getType(), s -> qClass.type.eq(Type.find(s)))
            .notNull(req.getStartTime(), qClass.createTime::ge).notNull(req.getEndTime(), qClass.createTime::lt)
            .notEmpty(req.getKeyword(), s -> {
                qClass.or().title.like("%" + s + "%").or().createPerson.like("%" + s + "%").or().responseBody.like(
                    "%" + s + "%");
            });

        return sysLogService.page(qClass, page, SysLogDtoMapper.INSTANCE::LogPageResp).map(R::ok);
    }

    /**
     * 导出
     *
     * @return
     */
    @GetMapping("export")
    public Mono<Void> export(LogPageReq req, ServerHttpResponse response) throws IOException {
        QSysLog qClass = new QSysLog().createTime.desc();
        LocalDateTime now = LocalDateTime.now();
        Where.ready().notEmpty(req.getType(), s -> qClass.type.eq(Type.find(s)));
        if (req.getTimeRange() == null) {
            if (req.getStartTime() != null && req.getEndTime() != null) {
                if (ChronoUnit.DAYS.between(req.getStartTime(), req.getEndTime()) > MIN_DAY) {
                    throw new BusinessException("不能导出大于" + MIN_DAY + "天的数据");
                }
            }
            if (req.getStartTime() != null) {
                if (ChronoUnit.DAYS.between(req.getStartTime(), now) > MIN_DAY) {
                    throw new BusinessException("不能导出大于" + MIN_DAY + "天的数据");
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
            if (ChronoUnit.DAYS.between(startTime, endTime) > MIN_DAY) {
                throw new BusinessException("不能导出大于" + MIN_DAY + "天的数据");
            }
            qClass.createTime.ge(startTime).createTime.lt(endTime);
        }

        return sysLogService.findAll(qClass).flatMap(logs -> {
            response.getHeaders()
                .add("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");

            List<LogExcelPojo> pojos = logs.stream().map(SysLogDtoMapper.INSTANCE::toLogExcelPojo)
                .collect(Collectors.toList());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, LogExcelPojo.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).sheet("系统日志").doWrite(pojos);
            try {
                outputStream.flush();
                byte[] bytes = outputStream.toByteArray();
                DataBuffer buffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(buffer));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }


    /**
     * 批量删除;只能清理90天之前的数据
     */
    @GetMapping("delete")
    @DeleteMapping("delete")
    public Mono<R<Boolean>> delete(IdsReq req) {
        BeanValidatorUtil.validate(req);
        return sysLogService.delete(new QSysLog().createTime.le(
            LocalDateTime.of(LocalDate.now().minusDays(MIN_DAY), LocalTime.of(0, 0, 0))).id.in(req.getId())).map(R::ok);
    }
}
