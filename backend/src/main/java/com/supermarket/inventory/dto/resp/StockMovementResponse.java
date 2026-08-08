package com.supermarket.inventory.dto.resp;

import com.supermarket.inventory.entity.MovementType;
import java.time.LocalDateTime;

public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productSku;
    private String productName;
    private MovementType type;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private String performedBy;
    private LocalDateTime createdAt;

    public StockMovementResponse() {
    }

    public StockMovementResponse(Long id, Long productId, String productSku, String productName, MovementType type, Integer quantity, Integer previousStock, Integer newStock, String reason, String performedBy, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.reason = reason;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
