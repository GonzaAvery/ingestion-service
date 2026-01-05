package com.health.ingestion.api;

import com.health.ingestion.api.dto.ErrorResponse;
import com.health.ingestion.api.dto.HealthEventRequest;
import com.health.ingestion.domain.HealthEvent;
import com.health.ingestion.kafka.HealthEventProducer;
import com.health.ingestion.service.EventNormalizationService;
import com.health.ingestion.service.MetricsService;
import com.health.ingestion.service.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";

    private final ValidationService validationService;
    private final EventNormalizationService normalizationService;
    private final HealthEventProducer eventProducer;
    private final MetricsService metricsService;

    public HealthEventController(
            ValidationService validationService,
            EventNormalizationService normalizationService,
            HealthEventProducer eventProducer,
            MetricsService metricsService) {
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.eventProducer = eventProducer;
        this.metricsService = metricsService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> ingestEvent(
            @Valid @RequestBody HealthEventRequest request,
            HttpServletRequest httpRequest) {

        String correlationId = (String) httpRequest.getAttribute(CORRELATION_ID_ATTRIBUTE);
        
        // Agregar campos al MDC para logging estructurado
        MDC.put("correlationId", correlationId);
        MDC.put("userId", request.getUserId());
        MDC.put("metricType", request.getMetricType());

        logger.info("Received health event request userId={} metricType={}", 
                request.getUserId(), request.getMetricType());

        // Validación adicional de reglas de negocio
        List<String> validationErrors = validationService.validate(request);

        if (!validationErrors.isEmpty()) {
            logger.warn("Validation failed userId={} metricType={} errors={} result=rejected", 
                    request.getUserId(), request.getMetricType(), validationErrors);
            
            metricsService.recordValidationError("business_validation");
            
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Unprocessable Entity",
                    "Validation failed",
                    validationErrors,
                    correlationId);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        }

        // Normalizar y publicar
        try {
            HealthEvent normalizedEvent = normalizationService.normalize(request, correlationId);
            
            // Agregar eventId al MDC después de la normalización
            MDC.put("eventId", normalizedEvent.getEventId());

            try {
                eventProducer.publish(normalizedEvent);
            } catch (HealthEventProducer.KafkaPublishException e) {
                logger.error("Kafka publish failed eventId={} userId={} correlationId={} result=rejected", 
                        normalizedEvent.getEventId(), request.getUserId(), correlationId, e);
                metricsService.recordKafkaPublishError();
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "Failed to publish event to Kafka");
                errorResponse.setCorrelationId(correlationId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // Registrar métrica de éxito
            metricsService.recordEventIngested(normalizedEvent.getMetricType());

            logger.info("Event ingested successfully eventId={} userId={} metricType={} result=accepted", 
                    normalizedEvent.getEventId(), request.getUserId(), normalizedEvent.getMetricType());

            return ResponseEntity.status(HttpStatus.ACCEPTED).build();

        } catch (Exception e) {
            logger.error("Unexpected error processing event userId={} correlationId={} result=rejected", 
                    request.getUserId(), correlationId, e);
            metricsService.recordSystemError("unexpected_error");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "Failed to process event");
            errorResponse.setCorrelationId(correlationId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        String correlationId = (String) request.getAttribute(CORRELATION_ID_ATTRIBUTE);
        MDC.put("correlationId", correlationId);
        
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.toList());

        logger.warn("Request validation failed correlationId={} errors={} result=rejected", 
                correlationId, errors);
        
        metricsService.recordValidationError("request_validation");
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid request payload",
                errors,
                correlationId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        String correlationId = (String) request.getAttribute(CORRELATION_ID_ATTRIBUTE);
        MDC.put("correlationId", correlationId);
        
        logger.warn("Malformed request payload correlationId={} result=rejected", correlationId, ex);
        
        metricsService.recordValidationError("malformed_payload");
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Malformed request payload: " + ex.getMessage());
        errorResponse.setCorrelationId(correlationId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
