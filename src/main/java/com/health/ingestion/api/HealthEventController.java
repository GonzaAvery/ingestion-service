package com.health.ingestion.api;

import com.health.ingestion.api.dto.ErrorResponse;
import com.health.ingestion.api.dto.HealthEventRequest;
import com.health.ingestion.domain.HealthEvent;
import com.health.ingestion.kafka.HealthEventProducer;
import com.health.ingestion.service.EventNormalizationService;
import com.health.ingestion.service.ValidationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/health")
public class HealthEventController {

    private static final Logger logger = LoggerFactory.getLogger(HealthEventController.class);

    private final ValidationService validationService;
    private final EventNormalizationService normalizationService;
    private final HealthEventProducer eventProducer;
    private final MeterRegistry meterRegistry;

    public HealthEventController(
            ValidationService validationService,
            EventNormalizationService normalizationService,
            HealthEventProducer eventProducer,
            MeterRegistry meterRegistry) {
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.eventProducer = eventProducer;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/events")
    public ResponseEntity<?> ingestEvent(@Valid @RequestBody HealthEventRequest request) {
        logger.debug("Received health event request for userId: {}, metricType: {}", 
                request.getUserId(), request.getMetricType());

        // Validación adicional de reglas de negocio
        List<String> validationErrors = validationService.validate(request);

        if (!validationErrors.isEmpty()) {
            logger.warn("Validation failed for userId: {}, errors: {}", 
                    request.getUserId(), validationErrors);
            Counter.builder("health.events.validation.errors")
                    .description("Total number of validation errors")
                    .register(meterRegistry)
                    .increment();
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "Unprocessable Entity",
                            "Validation failed",
                            validationErrors));
        }

        // Normalizar y publicar
        try {
            HealthEvent normalizedEvent = normalizationService.normalize(request);
            eventProducer.publish(normalizedEvent);

            // Registrar métrica por tipo de evento
            Counter.builder("health.events.ingested")
                    .description("Total number of health events ingested")
                    .tag("status", "success")
                    .tag("metricType", normalizedEvent.getMetricType())
                    .register(meterRegistry)
                    .increment();

            logger.info("Event ingested successfully for userId: {}, metricType: {}", 
                    request.getUserId(), request.getMetricType());

            return ResponseEntity.status(HttpStatus.ACCEPTED).build();

        } catch (Exception e) {
            logger.error("Error processing event for userId: {}", request.getUserId(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal Server Error",
                            "Failed to process event"));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.toList());

        Counter.builder("health.events.validation.errors")
                .description("Total number of validation errors")
                .register(meterRegistry)
                .increment();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        "Invalid request payload",
                        errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        logger.warn("Malformed request payload", ex);
        Counter.builder("health.events.validation.errors")
                .description("Total number of validation errors")
                .register(meterRegistry)
                .increment();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        "Malformed request payload: " + ex.getMessage()));
    }
}

