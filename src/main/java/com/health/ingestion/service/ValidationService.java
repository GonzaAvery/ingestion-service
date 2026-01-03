package com.health.ingestion.service;

import com.health.ingestion.api.dto.HealthEventRequest;
import com.health.ingestion.domain.MetricType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationService {

    private static final long MAX_TIMESTAMP_OFFSET_MS = 24 * 60 * 60 * 1000L; // 24 horas
    private static final long MIN_TIMESTAMP_OFFSET_MS = -24 * 60 * 60 * 1000L; // -24 horas

    public List<String> validate(HealthEventRequest request) {
        List<String> errors = new ArrayList<>();

        // Validar campos obligatorios (ya validados por @NotNull, pero verificamos por si acaso)
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            errors.add("userId is required and cannot be empty");
        }

        if (request.getTimestamp() == null) {
            errors.add("timestamp is required");
        } else {
            // Validar timestamp razonable
            long currentTime = Instant.now().toEpochMilli();
            long timestamp = request.getTimestamp();
            long offset = timestamp - currentTime;

            if (offset > MAX_TIMESTAMP_OFFSET_MS) {
                errors.add("timestamp cannot be more than 24 hours in the future");
            }
            if (offset < MIN_TIMESTAMP_OFFSET_MS) {
                errors.add("timestamp cannot be more than 24 hours in the past");
            }
        }

        if (request.getMetricType() == null || request.getMetricType().trim().isEmpty()) {
            errors.add("metricType is required and cannot be empty");
        } else {
            // Validar que el tipo de métrica sea válido
            MetricType type = MetricType.fromString(request.getMetricType());
            if (type == null) {
                errors.add("metricType must be one of: heart_rate, steps, sleep, hrv, calories");
            }
        }

        if (request.getValue() == null) {
            errors.add("value is required");
        } else {
            // Validar que el valor sea numérico válido
            if (Double.isNaN(request.getValue()) || Double.isInfinite(request.getValue())) {
                errors.add("value must be a valid number");
            } else {
                // Validar rangos por tipo de métrica
                MetricType type = MetricType.fromString(request.getMetricType());
                if (type != null) {
                    validateMetricRange(type, request.getValue(), errors);
                }
            }
        }

        if (request.getSource() == null || request.getSource().trim().isEmpty()) {
            errors.add("source is required and cannot be empty");
        }

        return errors;
    }

    private void validateMetricRange(MetricType type, Double value, List<String> errors) {
        switch (type) {
            case HEART_RATE:
                if (value < 30 || value > 220) {
                    errors.add("heart_rate value must be between 30 and 220 bpm");
                }
                break;
            case STEPS:
                if (value < 0 || value > 100000) {
                    errors.add("steps value must be between 0 and 100000");
                }
                break;
            case SLEEP:
                if (value < 0 || value > 24) {
                    errors.add("sleep value must be between 0 and 24 hours");
                }
                break;
            case HRV:
                if (value < 0 || value > 500) {
                    errors.add("hrv value must be between 0 and 500 ms");
                }
                break;
            case CALORIES:
                if (value < 0 || value > 10000) {
                    errors.add("calories value must be between 0 and 10000");
                }
                break;
        }
    }
}

