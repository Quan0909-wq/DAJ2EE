package com.example.webcinemabooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ đường dẫn ảo "/images/evidence/**" vào thư mục thật "/uploads/evidence/" trên máy tính
        String uploadPath = "file:///" + System.getProperty("user.dir") + "/uploads/evidence/";

        registry.addResourceHandler("/images/evidence/**")
                .addResourceLocations(uploadPath);
    }
}