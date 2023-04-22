package org.start2do.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.HttpLogDtoMapper;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.SqlPagePojo;
import org.start2do.dto.req.HttpLogDetailReq;
import org.start2do.dto.req.HttpLogPageReq;
import org.start2do.dto.resp.HttpLogPageResp;
import org.start2do.entity.HttpLog;
import org.start2do.entity.HttpLogId;
import org.start2do.entity.query.QHttpLog;
import org.start2do.mapper.HttpLogMapper;
import org.start2do.service.HttpLogService;
import org.start2do.util.BeanValidatorUtil;

/**
  *  请求日志
 */
@RequestMapping("/http/log")
@RestController
@RequiredArgsConstructor
public class HttpLogController {

    private final HttpLogService httpLogService;
    private final HttpLogMapper httpLogMapper;

    /**
      *  分页
     */
    @GetMapping("page")
    public R<Page<HttpLogPageResp>> page(HttpLogPageReq req) {
        SqlPagePojo pojo = HttpLogDtoMapper.INSTANCE.toSqlPojo(req);
        Long count = httpLogMapper.count(pojo);
        List<HttpLog> list = httpLogMapper.findAllBodyJson(pojo, req);
        return R.ok(new Page<>(count, req.getSize(), req.getCurrent(),
            list.stream().map(HttpLogDtoMapper.INSTANCE::toHttpLogPageResp).collect(Collectors.toList())));
    }

    /**
      *  日志详情
     */
    @GetMapping("detail")
    public R detail(HttpLogDetailReq req) {
        BeanValidatorUtil.validate(req);
        HttpLog httpLog = httpLogService.getOne(new QHttpLog().id.eq(new HttpLogId()));
        return R.ok();
    }
}
