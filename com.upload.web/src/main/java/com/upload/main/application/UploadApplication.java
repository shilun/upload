package com.upload.main.application;

import com.upload.util.constants.SystemConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.upload", "com.common.config"},excludeFilters = {})
public class UploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(UploadApplication.class, args);
    }
    @Bean("systemConstants")
    public SystemConstants constants() {
        return new SystemConstants();
    }
}


