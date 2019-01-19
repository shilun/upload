package com.upload.main.application;

import com.upload.util.constants.SystemConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {MongoDataAutoConfiguration.class})
@ComponentScan(basePackages = {"com.upload", "com.common.config"})
@EnableScheduling
public class UploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(UploadApplication.class, args);
    }
    @Bean("systemConstants")
    public SystemConstants constants() {
        return new SystemConstants();
    }
}


