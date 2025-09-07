package com.ecommerce.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
		System.out.println("API Gateway Started Successfully!");
        System.out.println("Gateway URL: http://localhost:8080");
        System.out.println("Product Service through Gateway: http://localhost:8080/api/products");
	}

}
