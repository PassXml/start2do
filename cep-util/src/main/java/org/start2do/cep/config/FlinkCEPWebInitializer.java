package org.start2do.cep.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlinkCEPWebInitializer implements ApplicationListener<ServletWebServerInitializedEvent> {
    private static final Logger log = LoggerFactory.getLogger(FlinkCEPWebInitializer.class);

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        ServletWebServerApplicationContext context = event.getApplicationContext();
        int port = event.getWebServer().getPort();
        
        log.info("Flink CEP HTTP event input is available on port {}", port);
        log.info("Event input endpoint: POST /cep/events");
        log.info("Batch event input endpoint: POST /cep/events/batch");
        log.info("Queue status endpoint: GET /cep/queue/status");
        log.info("Clear queue endpoint: POST /cep/queue/clear");
    }
}