package com.demo.ticket.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(ServiceWebConfig.class)
public class CommonWebAutoConfiguration {

    @Bean
    public TenantHeaderInterceptor tenantHeaderInterceptor() {
        return new TenantHeaderInterceptor();
    }
}
