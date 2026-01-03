package com.health.ingestion.service;

import com.health.ingestion.api.dto.HealthEventRequest;
import com.health.ingestion.domain.HealthEvent;
import org.springframework.stereotype.Service;

@Service
public class EventNormalizationService {

    public HealthEvent normalize(HealthEventRequest request) {
        HealthEvent event = new HealthEvent();
        event.setUserId(request.getUserId());
        event.setTimestamp(request.getTimestamp());
        event.setMetricType(request.getMetricType().toLowerCase());
        event.setValue(request.getValue());
        event.setSource(request.getSource());
        event.setUnit(request.getUnit());
        event.setAttributes(request.getAttributes());
        return event;
    }
}

