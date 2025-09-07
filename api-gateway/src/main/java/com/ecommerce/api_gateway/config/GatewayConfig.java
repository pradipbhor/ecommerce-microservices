package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Product Service Route
            .route("product-service", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("productServiceCB")
                        .setFallbackUri("forward:/fallback/products"))
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setMethods(HttpMethod.GET)))
                .uri("lb://PRODUCT-SERVICE"))
            
            // Product Service Categories Route
            .route("product-categories", r -> r
                .path("/api/categories/**")
                .filters(f -> f
                    .rewritePath("/api/categories/(?<segment>.*)", "/api/products/categories/${segment}"))
                .uri("lb://PRODUCT-SERVICE"))
            
            // Order Service Route (for future)
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("orderServiceCB")
                        .setFallbackUri("forward:/fallback/orders")))
                .uri("lb://ORDER-SERVICE"))
            
            // Cart Service Route (for future)
            .route("cart-service", r -> r
                .path("/api/cart/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("cartServiceCB")
                        .setFallbackUri("forward:/fallback/cart")))
                .uri("lb://CART-SERVICE"))
            
            // Payment Service Route (for future)
            .route("payment-service", r -> r
                .path("/api/payments/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("paymentServiceCB")
                        .setFallbackUri("forward:/fallback/payments")))
                .uri("lb://PAYMENT-SERVICE"))
            
            // Notification Service Route (for future)
            .route("notification-service", r -> r
                .path("/api/notifications/**")
                .uri("lb://NOTIFICATION-SERVICE"))
            
            .build();
    }
}