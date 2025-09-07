package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.ProductUpdateDTO;
import com.ecommerce.product.dto.StockUpdateDTO;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configure appropriately for production
public class ProductController {
    
    private final ProductService productService;
    
    // Create a new product
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to create Product: {}", productDTO.getName());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
    
    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("REST request to get Product by ID: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    // Get product by SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        log.info("REST request to get Product by SKU: {}", sku);
        ProductDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }
    
    // Get all products with pagination
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        log.info("REST request to get all Products - page: {}, size: {}", page, size);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get active products
    @GetMapping("/active")
    public ResponseEntity<Page<ProductDTO>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("REST request to get active Products");
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get products by category
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("REST request to get Products by category: {}", category);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Search products
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("REST request to search Products with query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO updateDTO) {
        
        log.info("REST request to update Product with ID: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, updateDTO);
        return ResponseEntity.ok(updatedProduct);
    }
    
    // Delete product (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("REST request to delete Product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    // Update stock
    @PutMapping("/{id}/stock")
    public ResponseEntity<Map<String, String>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateDTO stockUpdateDTO) {
        
        log.info("REST request to update stock for Product ID: {}", id);
        productService.updateStock(id, stockUpdateDTO);
        return ResponseEntity.ok(Map.of("message", "Stock updated successfully"));
    }
    
    // Get low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        log.info("REST request to get low stock Products with threshold: {}", threshold);
        List<ProductDTO> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    // Get products by price range
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        
        log.info("REST request to get Products in price range: {} - {}", minPrice, maxPrice);
        List<ProductDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    // Get all categories
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("REST request to get all categories");
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    // Check stock availability
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Map<String, Object>> checkStockAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        
        log.info("REST request to check stock availability for Product ID: {}, quantity: {}", id, quantity);
        boolean available = productService.checkStockAvailability(id, quantity);
        
        return ResponseEntity.ok(Map.of(
            "productId", id,
            "requestedQuantity", quantity,
            "available", available
        ));
    }
}