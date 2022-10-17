package org.start2do;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AuthApplication {

    @GetMapping("/")
    public String test() {
        return "Hello";
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
