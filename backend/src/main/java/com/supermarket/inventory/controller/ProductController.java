package com.supermarket.inventory.controller;

import com.supermarket.inventory.dto.req.ProductRequest;
import com.supermarket.inventory.dto.resp.ApiResponse;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "CRUD, search, filtering, and pagination for grocery products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Search & filter grocery products with pagination", description = "Supports searching by keyword, category, price bounds, low stock status, sorting, and pagination")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Page<ProductResponse> products = productService.getAllProducts(
                keyword, categoryId, minPrice, maxPrice, lowStock, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Fetches detailed information for a specific product")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by unique SKU", description = "Fetches detailed information for a specific SKU")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        ProductResponse product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new product", description = "Allowed for ADMIN and MANAGER roles")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update product details", description = "Allowed for ADMIN and MANAGER roles")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Allowed for ADMIN role only. Soft deletes product.")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}
