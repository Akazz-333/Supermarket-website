package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.ProductRequest;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.entity.Category;
import com.supermarket.inventory.entity.Product;
import com.supermarket.inventory.exception.DuplicateResourceException;
import com.supermarket.inventory.exception.ResourceNotFoundException;
import com.supermarket.inventory.repository.CategoryRepository;
import com.supermarket.inventory.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Dairy", "Milk products");
        testProduct = new Product(
                "SKU-MILK01",
                "Whole Milk",
                "Fresh milk",
                new BigDecimal("4.50"),
                20,
                5,
                testCategory,
                "Dairy Supplier",
                "12345"
        );
        testProduct.setId(10L);

        productRequest = new ProductRequest(
                "SKU-MILK01",
                "Whole Milk",
                "Fresh milk",
                new BigDecimal("4.50"),
                20,
                5,
                1L,
                "Dairy Supplier",
                "12345"
        );
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductById(10L);

        assertNotNull(response);
        assertEquals("SKU-MILK01", response.getSku());
        assertEquals("Whole Milk", response.getName());
        verify(productRepository, times(1)).findById(10L);
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void createProduct_Success() {
        when(productRepository.existsBySku("SKU-MILK01")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals("SKU-MILK01", response.getSku());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_DuplicateSku() {
        when(productRepository.existsBySku("SKU-MILK01")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(productRequest));
        verify(productRepository, never()).save(any(Product.class));
    }
}
