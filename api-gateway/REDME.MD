# API Gateway Service - Complete Implementation

## Step 1: Create API Gateway Project Structure

```
api-gateway/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── ecommerce/
│       │           └── gateway/
│       │               ├── ApiGatewayApplication.java
│       │               ├── config/
│       │               │   ├── GatewayConfig.java
│       │               │   └── RateLimiterConfig.java
│       │               └── controller/
│       │                   └── FallbackController.java
│       └── resources/
│           └── application.yml
```

## Step 2: Create pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.ecommerce</groupId>
    <artifactId>api-gateway</artifactId>
    <version>1.0.0</version>
    <name>api-gateway</name>
    <description>API Gateway for E-commerce Microservices</description>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        
        <!-- Eureka Client for Service Discovery -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        
        <!-- Circuit Breaker -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
        
        <!-- Redis for Rate Limiting (Optional - can skip for now) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        
        <!-- Actuator for monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Step 3: Create Main Application Class

### ApiGatewayApplication.java
```java
package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("API Gateway Started Successfully!");
        System.out.println("Gateway URL: http://localhost:8080");
        System.out.println("Product Service through Gateway: http://localhost:8080/api/products");
    }
}
```

## Step 4: Create Gateway Configuration

### config/GatewayConfig.java
```java
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
```

## Step 5: Create Rate Limiter Configuration (Optional)

### config/RateLimiterConfig.java
```java
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
```

## Step 6: Create Fallback Controller

### controller/FallbackController.java
```java
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
```

## Step 7: Create application.yml

### Without Redis (Simpler)
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
    
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    
# Circuit Breaker Configuration
resilience4j:
  circuitbreaker:
    instances:
      productServiceCB:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
      orderServiceCB:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
      cartServiceCB:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
      paymentServiceCB:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        
# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway
  endpoint:
    gateway:
      enabled: true
      
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.ecommerce.gateway: DEBUG
```

### With Redis for Rate Limiting (Optional)
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
    
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
      
      # Rate Limiter Configuration
      redis-rate-limiter:
        replenish-rate: 10  # Tokens per second
        burst-capacity: 20  # Maximum burst
        requested-tokens: 1
        
  redis:
    host: localhost
    port: 6379
    timeout: 10s
    
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    
# Rest of config same as above...
```

## Step 8: Build and Run

### 1. Build the API Gateway
```bash
cd api-gateway
mvn clean compile
```

### 2. Start Services in Order

**Terminal 1: Start Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
```

**Terminal 2: Start Product Service**
```bash
cd product-service
mvn spring-boot:run -Dspring.profiles.active=h2
```

**Terminal 3: Start API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
```

## Step 9: Test the API Gateway

### 1. Check Eureka Dashboard
Open: http://localhost:8761

You should see:
- API-GATEWAY registered
- PRODUCT-SERVICE registered

### 2. Test Product Service through Gateway

#### Direct Product Service Call (Port 8081):
```bash
curl http://localhost:8081/api/products
```

#### Through API Gateway (Port 8080):
```bash
curl http://localhost:8080/api/products
```

Both should return the same response!

### 3. Create a Product through Gateway
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PHONE001",
    "name": "Samsung Galaxy S24",
    "description": "Latest Samsung flagship",
    "price": 899.99,
    "quantity": 30,
    "category": "Electronics",
    "brand": "Samsung"
  }'
```

### 4. Get Products through Gateway
```bash
# Get all products
curl http://localhost:8080/api/products

# Get product by ID
curl http://localhost:8080/api/products/1

# Search products
curl "http://localhost:8080/api/products/search?query=Samsung"

# Get categories
curl http://localhost:8080/api/categories
```

### 5. Test Circuit Breaker

Stop the Product Service (Ctrl+C in Terminal 2), then try:
```bash
curl http://localhost:8080/api/products
```

You should get the fallback response:
```json
{
  "timestamp": "2025-09-08T...",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Product Service is temporarily unavailable. Please try again later.",
  "service": "Product Service"
}
```

## Step 10: Monitor Gateway Routes

### Check available routes:
```bash
curl http://localhost:8080/actuator/gateway/routes
```

### Check health:
```bash
curl http://localhost:8080/actuator/health
```

## What You've Achieved

✅ **API Gateway** routing requests to microservices  
✅ **Service Discovery** integration with Eureka  
✅ **Circuit Breaker** pattern for fault tolerance  
✅ **Fallback** responses when services are down  
✅ **Load Balancing** with `lb://` prefix  
✅ **CORS** configuration for frontend integration  
✅ **Actuator** endpoints for monitoring  

## Architecture So Far

```
Client → API Gateway (8080) → Eureka Server (8761)
                ↓
         Product Service (8081)
```

## Next Steps

You now have:
1. ✅ Product Service (with H2 database)
2. ✅ Eureka Server (Service Discovery)
3. ✅ API Gateway (with Circuit Breaker)

Next, we can build:
- **Cart Service** (with Redis for session storage)
- **Order Service** (with Saga pattern)
- **Payment Service** (payment processing)
- **Notification Service** (email/SMS)

Which would you like to implement n