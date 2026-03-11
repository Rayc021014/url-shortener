package com.urlshortener.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Serve Vue's built assets (JS, CSS, images) from the classpath static folder.
     * Run `npm run build` in /frontend then copy dist/ to src/main/resources/static/
     *
     * In development, the Vite dev server (port 5173) is used instead — this only
     * matters when running the Spring Boot jar with the built frontend embedded.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(31536000);  // 1 year cache for hashed assets

        registry.addResourceHandler("/favicon.svg", "/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400);
    }
}