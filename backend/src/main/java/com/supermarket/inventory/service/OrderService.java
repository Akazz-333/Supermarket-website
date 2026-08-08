package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.CreateOrderRequest;
import com.supermarket.inventory.dto.req.OrderItemRequest;
import com.supermarket.inventory.dto.req.OrderStatusUpdateRequest;
import com.supermarket.inventory.dto.resp.OrderItemResponse;
import com.supermarket.inventory.dto.resp.OrderResponse;
import com.supermarket.inventory.entity.*;
import com.supermarket.inventory.exception.BadRequestException;
import com.supermarket.inventory.exception.InsufficientStockException;
import com.supermarket.inventory.exception.ResourceNotFoundException;
import com.supermarket.inventory.repository.OrderRepository;
import com.supermarket.inventory.repository.ProductRepository;
import com.supermarket.inventory.repository.StockMovementRepository;
import com.supermarket.inventory.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final Random random = new Random();

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderNumber = generateOrderNumber();

        Order order = new Order(
                orderNumber,
                request.getCustomerName().trim(),
                request.getCustomerEmail(),
                request.getCustomerPhone(),
                request.getNotes()
        );

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            if (!product.getActive()) {
                throw new BadRequestException("Product '" + product.getName() + "' is inactive and cannot be ordered.");
            }

            OrderItem orderItem = new OrderItem(product, itemReq.getQuantity(), product.getPrice());
            order.addItem(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(
            OrderStatus status,
            String customerName,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (customerName != null && !customerName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("customerName")), "%" + customerName.trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable).map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus targetStatus = request.getStatus();

        if (currentStatus == targetStatus) {
            return mapToOrderResponse(order);
        }

        validateStatusTransition(currentStatus, targetStatus);

        // State Machine Actions:
        // 1. Transition to CONFIRMED: Deduct Stock
        if (targetStatus == OrderStatus.CONFIRMED && currentStatus == OrderStatus.PENDING) {
            deductOrderStock(order);
        }

        // 2. Transition to CANCELLED: Restore Stock (if previously confirmed or shipped)
        if (targetStatus == OrderStatus.CANCELLED && (currentStatus == OrderStatus.CONFIRMED || currentStatus == OrderStatus.SHIPPED)) {
            restoreOrderStock(order);
        }

        order.setStatus(targetStatus);
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    private void deductOrderStock(Order order) {
        String performedBy = getCurrentUsername();

        // Check availability first
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getAvailableStock() < item.getQuantity()) {
                throw new InsufficientStockException("Cannot confirm order " + order.getOrderNumber() + ". Insufficient stock for product '" + product.getName() + "'. Available: " + product.getAvailableStock() + ", Required: " + item.getQuantity());
            }
        }

        // Apply deductions
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int previousStock = product.getAvailableStock();
            int newStock = previousStock - item.getQuantity();
            product.setAvailableStock(newStock);
            productRepository.save(product);

            StockMovement movement = new StockMovement(
                    product,
                    MovementType.ORDER_DEDUCTION,
                    item.getQuantity(),
                    previousStock,
                    newStock,
                    "Stock deducted for confirmed order #" + order.getOrderNumber(),
                    performedBy
            );
            stockMovementRepository.save(movement);
        }
    }

    private void restoreOrderStock(Order order) {
        String performedBy = getCurrentUsername();

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int previousStock = product.getAvailableStock();
            int newStock = previousStock + item.getQuantity();
            product.setAvailableStock(newStock);
            productRepository.save(product);

            StockMovement movement = new StockMovement(
                    product,
                    MovementType.ORDER_RESTOCK,
                    item.getQuantity(),
                    previousStock,
                    newStock,
                    "Stock restored for cancelled order #" + order.getOrderNumber(),
                    performedBy
            );
            stockMovementRepository.save(movement);
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be modified or status changed.");
        }
        if (current == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled orders cannot be reopened.");
        }
        if (current == OrderStatus.PENDING && target == OrderStatus.DELIVERED) {
            throw new BadRequestException("Order must be CONFIRMED and SHIPPED before being DELIVERED.");
        }
        if (current == OrderStatus.SHIPPED && target == OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot revert a SHIPPED order back to CONFIRMED.");
        }
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = 10000 + random.nextInt(90000);
        return "ORD-" + datePrefix + "-" + randomNum;
    }

    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getSku(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getNotes(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
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
