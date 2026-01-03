package com.health.ingestion.domain;

public enum MetricType {
    HEART_RATE("heart_rate"),
    STEPS("steps"),
    SLEEP("sleep"),
    HRV("hrv"),
    CALORIES("calories");

    private final String value;

    MetricType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MetricType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (MetricType type : MetricType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}

