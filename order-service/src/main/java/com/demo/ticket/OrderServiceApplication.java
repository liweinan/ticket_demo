package com.demo.ticket;

import com.demo.ticket.config.CommonWebAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@Import(CommonWebAutoConfiguration.class)
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.demo.ticket.api", defaultConfiguration = com.demo.ticket.feign.FeignClientsConfiguration.class)
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
