package com.health.ingestion.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public class HealthEvent {

    @JsonProperty("schemaVersion")
    private String schemaVersion;

    @JsonProperty("ingestionTime")
    private Long ingestionTime;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("metricType")
    private String metricType;

    @JsonProperty("value")
    private Double value;

    @JsonProperty("source")
    private String source;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    public HealthEvent() {
        this.schemaVersion = "1.0";
        this.ingestionTime = Instant.now().toEpochMilli();
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Long getIngestionTime() {
        return ingestionTime;
    }

    public void setIngestionTime(Long ingestionTime) {
        this.ingestionTime = ingestionTime;
    }

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

