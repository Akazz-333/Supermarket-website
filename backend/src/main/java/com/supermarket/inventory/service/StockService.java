package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.StockOperationRequest;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.dto.resp.StockMovementResponse;
import com.supermarket.inventory.entity.MovementType;
import com.supermarket.inventory.entity.Product;
import com.supermarket.inventory.entity.StockMovement;
import com.supermarket.inventory.exception.InsufficientStockException;
import com.supermarket.inventory.exception.ResourceNotFoundException;
import com.supermarket.inventory.repository.ProductRepository;
import com.supermarket.inventory.repository.StockMovementRepository;
import com.supermarket.inventory.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductService productService;

    public StockService(ProductRepository productRepository, StockMovementRepository stockMovementRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productService = productService;
    }

    @Transactional
    public ProductResponse stockIn(StockOperationRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        int previousStock = product.getAvailableStock();
        int newStock = previousStock + request.getQuantity();
        product.setAvailableStock(newStock);
        productRepository.save(product);

        String performedBy = getCurrentUsername();
        String reason = request.getReason() != null ? request.getReason() : "Manual Stock-In Addition";

        StockMovement movement = new StockMovement(
                product,
                MovementType.STOCK_IN,
                request.getQuantity(),
                previousStock,
                newStock,
                reason,
                performedBy
        );
        stockMovementRepository.save(movement);

        return productService.mapToProductResponse(product);
    }

    @Transactional
    public ProductResponse stockOut(StockOperationRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        int previousStock = product.getAvailableStock();
        if (previousStock < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product '" + product.getName() + "'. Available: " + previousStock + ", Requested deduction: " + request.getQuantity());
        }

        int newStock = previousStock - request.getQuantity();
        product.setAvailableStock(newStock);
        productRepository.save(product);

        String performedBy = getCurrentUsername();
        String reason = request.getReason() != null ? request.getReason() : "Manual Stock-Out Deduction";

        StockMovement movement = new StockMovement(
                product,
                MovementType.STOCK_OUT,
                request.getQuantity(),
                previousStock,
                newStock,
                reason,
                performedBy
        );
        stockMovementRepository.save(movement);

        return productService.mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(productService::mapToProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getProductStockHistory(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(this::mapToStockMovementResponse);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getRecentMovements() {
        return stockMovementRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::mapToStockMovementResponse)
                .toList();
    }

    public StockMovementResponse mapToStockMovementResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getProduct().getName(),
                movement.getType(),
                movement.getQuantity(),
                movement.getPreviousStock(),
                movement.getNewStock(),
                movement.getReason(),
                movement.getPerformedBy(),
                movement.getCreatedAt()
        );
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUsername();
        }
        return "SYSTEM";
    }
}
