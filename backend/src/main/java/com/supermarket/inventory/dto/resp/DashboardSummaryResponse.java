package com.supermarket.inventory.dto.resp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardSummaryResponse {
    private long totalProducts;
    private long lowStockProductsCount;
    private long outOfStockProductsCount;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private Map<String, Long> ordersByStatus;
    private List<ProductResponse> lowStockAlerts;
    private List<StockMovementResponse> recentActivity;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long totalProducts, long lowStockProductsCount, long outOfStockProductsCount, long totalOrders, BigDecimal totalRevenue, Map<String, Long> ordersByStatus, List<ProductResponse> lowStockAlerts, List<StockMovementResponse> recentActivity) {
        this.totalProducts = totalProducts;
        this.lowStockProductsCount = lowStockProductsCount;
        this.outOfStockProductsCount = outOfStockProductsCount;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.ordersByStatus = ordersByStatus;
        this.lowStockAlerts = lowStockAlerts;
        this.recentActivity = recentActivity;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getLowStockProductsCount() {
        return lowStockProductsCount;
    }

    public void setLowStockProductsCount(long lowStockProductsCount) {
        this.lowStockProductsCount = lowStockProductsCount;
    }

    public long getOutOfStockProductsCount() {
        return outOfStockProductsCount;
    }

    public void setOutOfStockProductsCount(long outOfStockProductsCount) {
        this.outOfStockProductsCount = outOfStockProductsCount;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public List<ProductResponse> getLowStockAlerts() {
        return lowStockAlerts;
    }

    public void setLowStockAlerts(List<ProductResponse> lowStockAlerts) {
        this.lowStockAlerts = lowStockAlerts;
    }

    public List<StockMovementResponse> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<StockMovementResponse> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
