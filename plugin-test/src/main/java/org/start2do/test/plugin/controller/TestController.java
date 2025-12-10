package org.start2do.test.plugin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.test.plugin.service.TestService;


@RestController
@RequiredArgsConstructor
public class TestController  {

    private final TestService service;

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

}
