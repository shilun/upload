package com.upload.main.application;

import com.upload.util.constants.SystemConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.upload","com.common.config"})
public class UploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(UploadApplication.class, args);
    }

    @Value("${app.fileRootPath}")
    private String fileRootPath;

    @Bean("fileRootPath")
    public String fileRoot(){
        return fileRootPath;
    }

    @Bean("systemConstants")
    public SystemConstants constants(){
        return new SystemConstants();
    }
}