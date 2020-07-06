package com.upload.main.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                //.allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept", "X-Token", "content-type")
                .allowedHeaders("*")  //服务器允许的请求头
                .allowedMethods("POST", "PUT", "GET", "OPTIONS", "DELETE")  //服务器允许的请求方法
                .allowCredentials(true)  //允许带 cookie 的跨域请求
                .allowedOrigins("*")  //服务端允许哪些域请求资源
                .maxAge(3600);   //预检请求的缓存时间
    }
}
