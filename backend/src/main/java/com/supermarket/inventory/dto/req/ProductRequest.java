package com.supermarket.inventory.dto.req;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^SKU-[A-Z0-9]{4,15}$", message = "SKU must match format SKU-XXXXX (e.g. SKU-RICE01)")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Available stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer availableStock;

    @NotNull(message = "Minimum stock level threshold is required")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String supplierName;

    private String supplierContact;

    public ProductRequest() {
    }

    public ProductRequest(String sku, String name, String description, BigDecimal price, Integer availableStock, Integer minStockLevel, Long categoryId, String supplierName, String supplierContact) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.availableStock = availableStock;
        this.minStockLevel = minStockLevel;
        this.categoryId = categoryId;
        this.supplierName = supplierName;
        this.supplierContact = supplierContact;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierContact() {
        return supplierContact;
    }

    public void setSupplierContact(String supplierContact) {
        this.supplierContact = supplierContact;
    }
}
