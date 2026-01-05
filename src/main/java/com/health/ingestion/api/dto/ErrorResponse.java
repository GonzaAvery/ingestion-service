package com.health.ingestion.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("status")
    private int status;

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("correlationId")
    private String correlationId;

    public ErrorResponse(int status, String error, String message) {
        this.timestamp = Instant.now().toEpochMilli();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(int status, String error, String message, List<String> errors) {
        this.timestamp = Instant.now().toEpochMilli();
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = errors;
    }

    public ErrorResponse(int status, String error, String message, List<String> errors, String correlationId) {
        this.timestamp = Instant.now().toEpochMilli();
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = errors;
        this.correlationId = correlationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}

