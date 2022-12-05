package org.start2do;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestMain implements CommandLineRunner {

    public static void main(String[] args) {
        //根据SpringApplicationBuilder把web中的WebApplicationType设置为none
        new SpringApplicationBuilder(TestMain.class)
            .web(WebApplicationType.NONE)
            .run(args);

    }

    @Override
    public void run(String... args) throws Exception {
        Thread.currentThread().join();
    }
}
