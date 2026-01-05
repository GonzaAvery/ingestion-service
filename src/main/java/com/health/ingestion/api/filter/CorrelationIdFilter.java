package com.health.ingestion.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Agregar correlationId al MDC para logging estructurado
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // Agregar correlationId al response header para que el cliente pueda rastrearlo
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Agregar correlationId como atributo del request para que los controllers puedan accederlo
            request.setAttribute(CORRELATION_ID_MDC_KEY, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            // Limpiar MDC al finalizar el request
            MDC.clear();
        }
    }
}

