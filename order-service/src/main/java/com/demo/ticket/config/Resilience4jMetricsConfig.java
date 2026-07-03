package com.demo.ticket.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * 将 Feign CircuitBreaker / Retry 指标导出到 Micrometer / Prometheus。
 */
@Configuration
public class Resilience4jMetricsConfig {

    public Resilience4jMetricsConfig(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            MeterRegistry meterRegistry) {
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry)
                .bindTo(meterRegistry);
        TaggedRetryMetrics.ofRetryRegistry(retryRegistry)
                .bindTo(meterRegistry);
    }
}
