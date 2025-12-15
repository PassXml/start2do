package com.start2do.test.plugin.controller;

import com.start2do.test.plugin.mapper.TestMapper;
import com.start2do.test.plugin.service.Test2Service;
import lombok.RequiredArgsConstructor;
import org.start2do.plugin.api.spring.annotation.PluginController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@PluginController
@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestMapper testMapper;
    private final Test2Service service;

    @GetMapping("test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(service.version());
    }

    @PostMapping("abc")
    public ResponseEntity<String> abc(@RequestBody String body) {
        return ResponseEntity.ok(body);
    }

    @GetMapping("abc")
    public ResponseEntity<String> abcget(@RequestBody String body) {
        return ResponseEntity.ok("是新接口2025年12月10日12:37:36-----123123");
    }

    @GetMapping("mybatis")
    public ResponseEntity<String> mybatis() {
        return ResponseEntity.ok(testMapper.version() );
    }
}
