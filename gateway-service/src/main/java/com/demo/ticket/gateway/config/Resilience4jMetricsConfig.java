package com.demo.ticket.gateway.config;

import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 将 Gateway 租户 RateLimiter 指标导出到 Micrometer / Prometheus。
 */
@Configuration
public class Resilience4jMetricsConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.ofDefaults();
    }

    @Bean
    public TaggedRateLimiterMetrics taggedRateLimiterMetrics(
            RateLimiterRegistry rateLimiterRegistry,
            MeterRegistry meterRegistry) {
        TaggedRateLimiterMetrics metrics =
                TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }
}
