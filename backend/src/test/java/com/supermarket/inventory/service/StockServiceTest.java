package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.StockOperationRequest;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.entity.Category;
import com.supermarket.inventory.entity.Product;
import com.supermarket.inventory.entity.StockMovement;
import com.supermarket.inventory.exception.InsufficientStockException;
import com.supermarket.inventory.repository.ProductRepository;
import com.supermarket.inventory.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private StockService stockService;

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Beverages", "Drinks");
        product = new Product(
                "SKU-JUIC01",
                "Orange Juice",
                "Fresh juice",
                new BigDecimal("5.00"),
                10,
                5,
                category,
                "Juice Co",
                "555-1234"
        );
        product.setId(1L);
    }

    @Test
    void stockIn_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        StockOperationRequest request = new StockOperationRequest(1L, 15, "Restock order");
        stockService.stockIn(request);

        assertEquals(25, product.getAvailableStock());
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void stockOut_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        StockOperationRequest request = new StockOperationRequest(1L, 4, "Store sale");
        stockService.stockOut(request);

        assertEquals(6, product.getAvailableStock());
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void stockOut_InsufficientStock_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        StockOperationRequest request = new StockOperationRequest(1L, 20, "Excessive sale");

        assertThrows(InsufficientStockException.class, () -> stockService.stockOut(request));
        assertEquals(10, product.getAvailableStock()); // Stock remains unchanged
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }
}
