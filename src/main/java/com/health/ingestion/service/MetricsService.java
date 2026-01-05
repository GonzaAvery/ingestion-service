package com.health.ingestion.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordEventIngested(String metricType) {
        Counter.builder("health.events.ingested")
                .description("Total number of health events ingested successfully")
                .tag("status", "success")
                .tag("metricType", metricType)
                .register(meterRegistry)
                .increment();
    }

    public void recordValidationError(String errorType) {
        Counter.builder("health.events.validation.errors")
                .description("Total number of validation errors")
                .tag("errorType", errorType)
                .register(meterRegistry)
                .increment();
    }

    public void recordKafkaPublishError() {
        Counter.builder("health.events.kafka.errors")
                .description("Total number of Kafka publish errors")
                .tag("errorType", "publish_failure")
                .register(meterRegistry)
                .increment();
    }

    public void recordSystemError(String errorType) {
        Counter.builder("health.events.system.errors")
                .description("Total number of system errors")
                .tag("errorType", errorType)
                .register(meterRegistry)
                .increment();
    }
}

