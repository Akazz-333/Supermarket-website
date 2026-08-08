package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.CreateOrderRequest;
import com.supermarket.inventory.dto.req.OrderItemRequest;
import com.supermarket.inventory.dto.req.OrderStatusUpdateRequest;
import com.supermarket.inventory.dto.resp.OrderResponse;
import com.supermarket.inventory.entity.*;
import com.supermarket.inventory.exception.InsufficientStockException;
import com.supermarket.inventory.repository.OrderRepository;
import com.supermarket.inventory.repository.ProductRepository;
import com.supermarket.inventory.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private OrderService orderService;

    private Product product;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Grains", "Cereal grains");
        product = new Product("SKU-RICE01", "Basmati Rice", "Rice", new BigDecimal("10.00"), 50, 5, category, "Supplier", "123");
        product.setId(1L);

        pendingOrder = new Order("ORD-20260808-10001", "John Doe", "john@example.com", "123456", "Test order");
        pendingOrder.setId(100L);
        pendingOrder.addItem(new OrderItem(product, 5, new BigDecimal("10.00")));
        pendingOrder.setStatus(OrderStatus.PENDING);
    }

    @Test
    void createOrder_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "John Doe",
                "john@example.com",
                "123456",
                "Test order",
                List.of(new OrderItemRequest(1L, 5))
        );

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("50.00"), response.getTotalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void confirmOrder_DeductsStock() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest(OrderStatus.CONFIRMED);
        orderService.updateOrderStatus(100L, updateRequest);

        assertEquals(45, product.getAvailableStock()); // 50 - 5 = 45
        assertEquals(OrderStatus.CONFIRMED, pendingOrder.getStatus());
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void confirmOrder_InsufficientStock_ThrowsException() {
        product.setAvailableStock(2); // Only 2 in stock, order asks for 5
        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest(OrderStatus.CONFIRMED);

        assertThrows(InsufficientStockException.class, () -> orderService.updateOrderStatus(100L, updateRequest));
        assertEquals(2, product.getAvailableStock()); // Unchanged
        assertEquals(OrderStatus.PENDING, pendingOrder.getStatus()); // Status unchanged
    }

    @Test
    void cancelConfirmedOrder_RestoresStock() {
        pendingOrder.setStatus(OrderStatus.CONFIRMED);
        product.setAvailableStock(45); // Deducted previously

        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest(OrderStatus.CANCELLED);
        orderService.updateOrderStatus(100L, updateRequest);

        assertEquals(50, product.getAvailableStock()); // Restored 45 + 5 = 50
        assertEquals(OrderStatus.CANCELLED, pendingOrder.getStatus());
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }
}
