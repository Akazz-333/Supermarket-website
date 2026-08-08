package com.supermarket.inventory.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockOperationRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String reason;

    public StockOperationRequest() {
    }

    public StockOperationRequest(Long productId, Integer quantity, String reason) {
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
