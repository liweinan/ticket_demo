package com.demo.ticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 微服务 Web 配置：CORS + 租户 Header 拦截器。
 */
@Configuration
public class ServiceWebConfig implements WebMvcConfigurer {

    private final TenantHeaderInterceptor tenantHeaderInterceptor;

    public ServiceWebConfig(TenantHeaderInterceptor tenantHeaderInterceptor) {
        this.tenantHeaderInterceptor = tenantHeaderInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:51730",
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:51730",
                        "http://frontend:5173",
                        "http://host.docker.internal:5173",
                        "http://host.docker.internal:51730")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Tenant-ID");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantHeaderInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/health/**",
                        "/api/tenants",
                        "/api/tenants/**",
                        "/internal/**",
                        "/actuator/**");
    }
}
