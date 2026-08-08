package com.supermarket.inventory.controller;

import com.supermarket.inventory.dto.req.CreateOrderRequest;
import com.supermarket.inventory.dto.req.OrderStatusUpdateRequest;
import com.supermarket.inventory.dto.resp.ApiResponse;
import com.supermarket.inventory.dto.resp.OrderResponse;
import com.supermarket.inventory.entity.OrderStatus;
import com.supermarket.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order & Lifecycle Management", description = "Order creation, item details, status workflows (PENDING -> CONFIRMED -> SHIPPED -> DELIVERED / CANCELLED), auto-stock deduction and restoration")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create customer grocery order", description = "Creates a new PENDING order with multiple grocery items")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(ApiResponse.success("Order created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all orders with filtering & pagination", description = "Filter by order status, customer name, page, size, and sorting")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<OrderResponse> orders = orderService.getAllOrders(status, customerName, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID", description = "Fetches complete order details including item breakdown")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order details by Order Number", description = "Fetches complete order details using order number (e.g. ORD-20260808-12345)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order lifecycle status", description = "Transitions order status. CONFIRMED automatically deducts stock; CANCELLED automatically restores stock.")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated to " + request.getStatus(), response));
    }
}
