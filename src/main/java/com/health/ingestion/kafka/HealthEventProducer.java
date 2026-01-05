package com.health.ingestion.kafka;

import com.health.ingestion.domain.HealthEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class HealthEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(HealthEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public HealthEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(HealthEvent event) throws KafkaPublishException {
        try {
            String message = objectMapper.writeValueAsString(event);
            String key = event.getUserId();

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Event published successfully eventId={} userId={} offset={}", 
                            event.getEventId(), key, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish event eventId={} userId={} correlationId={}", 
                            event.getEventId(), key, event.getCorrelationId(), ex);
                }
            });

        } catch (JsonProcessingException e) {
            logger.error("Error serializing event eventId={} userId={} correlationId={}", 
                    event.getEventId(), event.getUserId(), event.getCorrelationId(), e);
            throw new KafkaPublishException("Failed to serialize event", e);
        }
    }

    public static class KafkaPublishException extends RuntimeException {
        public KafkaPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

