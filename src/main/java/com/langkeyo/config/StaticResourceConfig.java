package com.langkeyo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地静态资源映射（最小可用方案）
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将项目根目录 uploads 下文件映射为可访问静态资源
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}

