package com.supermarket.inventory.controller;

import com.supermarket.inventory.dto.req.StockOperationRequest;
import com.supermarket.inventory.dto.resp.ApiResponse;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.dto.resp.StockMovementResponse;
import com.supermarket.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@Tag(name = "Stock & Inventory Operations", description = "Stock addition (Stock-In), deduction (Stock-Out), movement audit trail, and low-stock alerts")
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/stock-in")
    @Operation(summary = "Add stock (Stock-In)", description = "Increases product inventory quantity and logs stock movement")
    public ResponseEntity<ApiResponse<ProductResponse>> stockIn(@Valid @RequestBody StockOperationRequest request) {
        ProductResponse response = stockService.stockIn(request);
        return ResponseEntity.ok(ApiResponse.success("Stock added successfully", response));
    }

    @PostMapping("/stock-out")
    @Operation(summary = "Deduct stock (Stock-Out)", description = "Deducts product inventory quantity. Prevents negative stock.")
    public ResponseEntity<ApiResponse<ProductResponse>> stockOut(@Valid @RequestBody StockOperationRequest request) {
        ProductResponse response = stockService.stockOut(request);
        return ResponseEntity.ok(ApiResponse.success("Stock deducted successfully", response));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products below low-stock threshold", description = "Returns all active products where availableStock <= minStockLevel")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        List<ProductResponse> products = stockService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", products));
    }

    @GetMapping("/product/{productId}/movements")
    @Operation(summary = "Get stock movement history for a product", description = "Paginated audit trail of all inventory changes for a specific product")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getProductStockHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<StockMovementResponse> history = stockService.getProductStockHistory(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Product stock movements retrieved successfully", history));
    }

    @GetMapping("/movements/recent")
    @Operation(summary = "Get recent stock movements across supermarket", description = "Top 20 most recent stock operations")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getRecentMovements() {
        List<StockMovementResponse> movements = stockService.getRecentMovements();
        return ResponseEntity.ok(ApiResponse.success("Recent stock movements retrieved successfully", movements));
    }
}
