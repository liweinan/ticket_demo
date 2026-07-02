package com.demo.ticket.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientsConfiguration {

    @Bean
    public TenantFeignInterceptor tenantFeignInterceptor() {
        return new TenantFeignInterceptor();
    }
}
