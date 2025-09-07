package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        // Rate limit by user ID from header
        return exchange -> Mono.just(
            exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id") != null ? 
                exchange.getRequest().getHeaders().getFirst("X-User-Id") : 
                "anonymous"
        );
    }
    
    @Bean
    public KeyResolver ipKeyResolver() {
        // Rate limit by IP address
        return exchange -> Mono.just(
            exchange.getRequest()
                .getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
                "unknown"
        );
    }
}