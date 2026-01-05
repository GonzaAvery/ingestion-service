package com.health.ingestion.service;

import com.health.ingestion.api.dto.HealthEventRequest;
import com.health.ingestion.domain.HealthEvent;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventNormalizationService {

    public HealthEvent normalize(HealthEventRequest request, String correlationId) {
        HealthEvent event = new HealthEvent();
        
        // Generar eventId único
        String eventId = UUID.randomUUID().toString();
        event.setEventId(eventId);
        
        // Establecer correlationId
        event.setCorrelationId(correlationId);
        
        // Agregar eventId al MDC para logging estructurado
        MDC.put("eventId", eventId);
        
        // Copiar datos del request
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

