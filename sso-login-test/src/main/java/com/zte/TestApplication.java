package com.zte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
//@EnableWebSecurity
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestApplication {

    @GetMapping("/")
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "当前用户信息：" + auth.getPrincipal();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class);
    }
}
