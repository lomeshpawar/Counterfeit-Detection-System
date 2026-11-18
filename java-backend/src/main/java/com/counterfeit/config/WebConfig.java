package com.counterfeit.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures static upload resources and the HTTP client used to call the AI service.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        java.io.File folder = new java.io.File(uploadDir);
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
     * Creates a RestTemplate with finite timeouts so an unavailable or stalled AI
     * service cannot hold backend request threads indefinitely.
     *
     * The defaults allow a short connection window and a longer response window
     * for CPU-bound image inference. Both values can be overridden through
     * AI_SERVICE_CONNECT_TIMEOUT and AI_SERVICE_READ_TIMEOUT.
     */
    @Bean
    public RestTemplate restTemplate(
            @Value("${ai.service.connect-timeout:3s}") Duration connectTimeout,
            @Value("${ai.service.read-timeout:60s}") Duration readTimeout) {

        if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()) {
            throw new IllegalArgumentException("AI service connect timeout must be greater than zero.");
        }
        if (readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
            throw new IllegalArgumentException("AI service read timeout must be greater than zero.");
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return new RestTemplate(requestFactory);
    }
}
