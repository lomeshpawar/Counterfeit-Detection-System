package com.counterfeit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * WebMvc Configuration class.
 * Configures static resource handlers (to serve uploaded product images via HTTP)
 * and provides a RestTemplate bean for calling external microservices (like Flask AI Service).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads/}")
    private String uploadDir;

    /**
     * Exposes the local uploads folder as a static web resource under /uploads/**
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String absolutePath = folder.getAbsolutePath().replace("\\", "/");
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + absolutePath);
    }

    /**
     * Creates a RestTemplate bean for making HTTP REST calls to the Flask AI service.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
