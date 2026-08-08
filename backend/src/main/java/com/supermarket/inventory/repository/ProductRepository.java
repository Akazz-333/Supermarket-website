package com.supermarket.inventory.repository;

import com.supermarket.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySku(String sku);
    Boolean existsBySku(String sku);
    Boolean existsByCategoryId(Long categoryId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.availableStock <= p.minStockLevel")
    long countLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.availableStock = 0")
    long countOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.availableStock <= p.minStockLevel")
    List<Product> findLowStockProducts();
}
