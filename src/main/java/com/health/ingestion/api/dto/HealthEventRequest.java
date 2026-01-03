package com.health.ingestion.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public class HealthEventRequest {

    @NotNull(message = "userId is required")
    @JsonProperty("userId")
    private String userId;

    @NotNull(message = "timestamp is required")
    @Positive(message = "timestamp must be positive")
    @JsonProperty("timestamp")
    private Long timestamp;

    @NotNull(message = "metricType is required")
    @JsonProperty("metricType")
    private String metricType;

    @NotNull(message = "value is required")
    @JsonProperty("value")
    private Double value;

    @NotNull(message = "source is required")
    @JsonProperty("source")
    private String source;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}

