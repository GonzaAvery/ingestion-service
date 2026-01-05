package com.health.ingestion.api.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
            CorrelationIdFilter correlationIdFilter) {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(correlationIdFilter);
        registration.addUrlPatterns("/v1/health/*");
        registration.setOrder(1);
        return registration;
    }
}

