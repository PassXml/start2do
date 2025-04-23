package org.start2do.bpm.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.start2do.util.BeanValidatorUtil;

@SpringBootApplication
public class RunMain {

    public static void main(String[] args) {
        BeanValidatorUtil.setEchoPath();
        SpringApplication.run(RunMain.class, args);
    }
}
