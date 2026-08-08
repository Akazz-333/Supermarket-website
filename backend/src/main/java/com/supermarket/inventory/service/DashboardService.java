package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.resp.DashboardSummaryResponse;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.dto.resp.StockMovementResponse;
import com.supermarket.inventory.entity.OrderStatus;
import com.supermarket.inventory.repository.OrderRepository;
import com.supermarket.inventory.repository.ProductRepository;
import com.supermarket.inventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductService productService;
    private final StockService stockService;

    public DashboardService(ProductRepository productRepository, OrderRepository orderRepository, StockMovementRepository stockMovementRepository, ProductService productService, StockService stockService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productService = productService;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long totalProducts = productRepository.count();
        long lowStockCount = productRepository.countLowStockProducts();
        long outOfStockCount = productRepository.countOutOfStockProducts();

        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
        }

        List<ProductResponse> lowStockAlerts = stockService.getLowStockProducts();
        List<StockMovementResponse> recentActivity = stockService.getRecentMovements();

        return new DashboardSummaryResponse(
                totalProducts,
                lowStockCount,
                outOfStockCount,
                totalOrders,
                totalRevenue,
                ordersByStatus,
                lowStockAlerts,
                recentActivity
        );
    }
}
