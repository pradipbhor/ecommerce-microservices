package com.ecommerce.product.service.impl;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.ProductUpdateDTO;
import com.ecommerce.product.dto.StockUpdateDTO;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.DuplicateSkuException;
import com.ecommerce.product.exception.InsufficientStockException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with SKU: {}", productDTO.getSku());
        
        // Check if SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateSkuException(productDTO.getSku());
        }
        
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return convertToDTO(savedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return convertToDTO(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return convertToDTO(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products, page: {}, size: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable).map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getActiveProducts(Pageable pageable) {
        log.debug("Fetching active products");
        return productRepository.findByIsActiveTrue(pageable).map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategoryAndIsActiveTrue(category, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);
        return productRepository.searchProducts(searchTerm, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public ProductDTO updateProduct(Long id, ProductUpdateDTO updateDTO) {
        log.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Update only non-null fields
        if (updateDTO.getName() != null) {
            product.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPrice() != null) {
            product.setPrice(updateDTO.getPrice());
        }
        if (updateDTO.getQuantity() != null) {
            product.setQuantity(updateDTO.getQuantity());
        }
        if (updateDTO.getCategory() != null) {
            product.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getBrand() != null) {
            product.setBrand(updateDTO.getBrand());
        }
        if (updateDTO.getImageUrl() != null) {
            product.setImageUrl(updateDTO.getImageUrl());
        }
        if (updateDTO.getIsActive() != null) {
            product.setIsActive(updateDTO.getIsActive());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", id);
        
        return convertToDTO(updatedProduct);
    }
    
    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Soft delete - just mark as inactive
        product.setIsActive(false);
        productRepository.save(product);
        
        log.info("Product marked as inactive with ID: {}", id);
    }
    
    @Override
    public void updateStock(Long id, StockUpdateDTO stockUpdateDTO) {
        log.info("Updating stock for product ID: {}, operation: {}, quantity: {}", 
                 id, stockUpdateDTO.getOperationType(), stockUpdateDTO.getQuantity());
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        if (stockUpdateDTO.getOperationType() == StockUpdateDTO.OperationType.ADD) {
            product.setQuantity(product.getQuantity() + stockUpdateDTO.getQuantity());
        } else {
            if (product.getQuantity() < stockUpdateDTO.getQuantity()) {
                throw new InsufficientStockException(id, 
                        stockUpdateDTO.getQuantity(), product.getQuantity());
            }
            product.setQuantity(product.getQuantity() - stockUpdateDTO.getQuantity());
        }
        
        productRepository.save(product);
        log.info("Stock updated successfully for product ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        log.debug("Fetching products with stock below: {}", threshold);
        return productRepository.findLowStockProducts(threshold).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching products in price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.debug("Fetching all categories");
        return productRepository.findAllCategories();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return product.getQuantity() >= quantity;
    }
    
    // Helper methods
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    private Product convertToEntity(ProductDTO dto) {
        return Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .category(dto.getCategory())
                .brand(dto.getBrand())
                .imageUrl(dto.getImageUrl())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}