package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.ProductUpdateDTO;
import com.ecommerce.product.dto.StockUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    ProductDTO getProductBySku(String sku);
    Page<ProductDTO> getAllProducts(Pageable pageable);
    Page<ProductDTO> getActiveProducts(Pageable pageable);
    Page<ProductDTO> getProductsByCategory(String category, Pageable pageable);
    Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable);
    ProductDTO updateProduct(Long id, ProductUpdateDTO updateDTO);
    void deleteProduct(Long id);
    void updateStock(Long id, StockUpdateDTO stockUpdateDTO);
    List<ProductDTO> getLowStockProducts(Integer threshold);
    List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<String> getAllCategories();
    boolean checkStockAvailability(Long productId, Integer quantity);
}