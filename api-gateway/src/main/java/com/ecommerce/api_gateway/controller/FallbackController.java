package com.ecommerce.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> productServiceFallback() {
        return createFallbackResponse("Product Service");
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> orderServiceFallback() {
        return createFallbackResponse("Order Service");
    }
    
    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> cartServiceFallback() {
        return createFallbackResponse("Cart Service");
    }
    
    @GetMapping("/payments")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return createFallbackResponse("Payment Service");
    }
    
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is temporarily unavailable. Please try again later.");
        response.put("service", serviceName);
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response);
    }
}