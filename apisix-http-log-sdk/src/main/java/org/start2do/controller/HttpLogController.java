package org.start2do.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.req.HttpLogDetailReq;
import org.start2do.service.HttpLogService;
import org.start2do.util.BeanValidatorUtil;

@RequestMapping("/http/log")
@RestController
@RequiredArgsConstructor

public class HttpLogController {

    private final HttpLogService httpLogService;

    @GetMapping("page")
    public R<Page<>> page() {

    }

    @GetMapping("detail")
    public R detail(HttpLogDetailReq req) {
        BeanValidatorUtil.validate(req);
        return R.ok;
    }
}
