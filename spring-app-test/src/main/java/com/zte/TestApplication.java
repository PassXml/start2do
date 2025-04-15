package com.zte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
//@EnableWebSecurity
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestApplication {


    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class);
    }
}
